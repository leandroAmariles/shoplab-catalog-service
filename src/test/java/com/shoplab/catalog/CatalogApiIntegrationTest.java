package com.shoplab.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoplab.catalog.support.TestDatabase;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/** The REST API and the MCP server over a real PostgreSQL migrated by Flyway. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcClient jdbc;

    private WebTestClient client;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        var postgres = TestDatabase.instance();
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl() + "&currentSchema=catalog");
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void seed() {
        client = WebTestClient.bindToServer().baseUrl("http://127.0.0.1:" + port).build();
        jdbc.sql("TRUNCATE products, product_categories, sellers").update();
        jdbc.sql("INSERT INTO product_categories VALUES ('relogios_presentes', 'watches_gifts'), ('perfumaria', NULL)")
                .update();
        jdbc.sql("""
                INSERT INTO products (product_id, category_name, weight_g, length_cm, height_cm, width_cm,
                                      reference_price, stock)
                VALUES ('c3', 'perfumaria', 100, 1, 2, 3, 10.00, 4),
                       ('a1', 'relogios_presentes', 200, 4, 5, 6, 99.90, 7),
                       ('b2', 'relogios_presentes', NULL, NULL, NULL, NULL, NULL, 0)
                """).update();
    }

    @Test
    void pagesAreOrderedByProductIdWithoutOverlap() {
        client.get().uri("/api/catalog/products?page=0&size=2").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].productId").isEqualTo("a1")
                .jsonPath("$.content[1].productId").isEqualTo("b2")
                .jsonPath("$.totalElements").isEqualTo(3)
                .jsonPath("$.totalPages").isEqualTo(2)
                .jsonPath("$.last").isEqualTo(false);
        client.get().uri("/api/catalog/products?page=1&size=2").exchange()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].productId").isEqualTo("c3")
                .jsonPath("$.last").isEqualTo(true);
    }

    @Test
    void aPageBeyondTheEndIsEmpty() {
        client.get().uri("/api/catalog/products?page=2000000000&size=200").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(0)
                .jsonPath("$.totalElements").isEqualTo(3);
    }

    @Test
    void filtersByCategoryWordsAndStock() {
        assertTotal("category=watches_gifts", 2);
        assertTotal("category=perfumaria", 1);
        assertTotal("query=watches gifts", 2);
        assertTotal("query=watches&inStock=true", 1);
        // Wildcards and quotes are text, not SQL.
        assertTotal("query=%", 0);
        assertTotal("query=o'reilly", 0);
    }

    @Test
    void servesDetailStockAndCategories() {
        client.get().uri("/api/catalog/products/a1").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.categoryNameEn").isEqualTo("watches_gifts")
                .jsonPath("$.dimensions.widthCm").isEqualTo(6)
                .jsonPath("$.available").isEqualTo(true);
        client.get().uri("/api/catalog/products/b2/stock").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.stock").isEqualTo(0)
                .jsonPath("$.available").isEqualTo(false)
                .jsonPath("$.updatedAt").exists();
        client.get().uri("/api/catalog/categories").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("perfumaria")
                .jsonPath("$[1].nameEn").isEqualTo("watches_gifts");
    }

    @Test
    void errorsAreTypedAndCorrelated() {
        client.get().uri("/api/catalog/products/missing").header("X-Request-Id", "req-1").exchange()
                .expectStatus().isNotFound()
                .expectHeader().valueEquals("X-Request-Id", "req-1")
                .expectBody().jsonPath("$.code").isEqualTo("SLHD_CATALOG_00001");
        client.get().uri("/api/catalog/products/missing/stock").exchange().expectStatus().isNotFound();
        for (String invalid : List.of("page=-1", "size=0", "size=201", "page=abc")) {
            client.get().uri("/api/catalog/products?" + invalid).exchange()
                    .expectStatus().isEqualTo(422)
                    .expectBody().jsonPath("$.code").isEqualTo("SLHD_CATALOG_00002");
        }
        client.get().uri("/api/catalog/nothing").exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.code").isEqualTo("SLHD_CATALOG_00005");
        client.delete().uri("/api/catalog/categories").exchange()
                .expectStatus().isEqualTo(405)
                .expectBody().jsonPath("$.code").isEqualTo("SLHD_CATALOG_00004");
    }

    @Test
    void mcpHandshakeAndToolList() {
        assertThat(rpc("initialize", Map.of()).get("result").toString()).contains("protocolVersion");
        assertThat(rpc("ping", Map.of()).get("result")).isEqualTo(Map.of());
        assertThat(rpc("tools/list", Map.of()).get("result").toString())
                .contains("searchProducts", "getProductDetails", "in_stock_only");
        assertThat(rpc("resources/list", Map.of())).containsKey("error");
        client.post().uri("/mcp").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("jsonrpc", "2.0", "method", "notifications/initialized"))
                .exchange().expectStatus().isAccepted();
        var first = client.get().uri("/mcp/sse").exchange().expectStatus().isOk()
                .returnResult(String.class).getResponseBody().blockFirst();
        assertThat(first).isEqualTo("/mcp/messages");
    }

    @Test
    void mcpToolsAnswerForTheAgent() {
        assertThat(call("searchProducts", Map.of("query", "watches"))).contains("a1", "false").doesNotContain("b2");
        assertThat(call("search_products", Map.of("query", "watches", "in_stock_only", false, "limit", 50)))
                .contains("b2", "AGOTADO");
        assertThat(call("searchProducts", Map.of("query", "nothing"))).contains("No se encontraron", "isError=false");
        assertThat(call("getProductDetails", Map.of("product_id", "a1"))).contains("99.90", "4x5x6");
        // A product without measures says nothing about them.
        assertThat(call("getProductDetails", Map.of("product_id", "b2")))
                .contains("AGOTADO").doesNotContain("null", "Dimensiones");
        assertThat(call("get_product_details", Map.of("product_id", "zz")))
                .contains("No se encontró", "isError=false");
        assertThat(call("searchProducts", Map.of())).contains("SLHD_CATALOG_00002", "isError=true");
        assertThat(call("getProductDetails", Map.of())).contains("SLHD_CATALOG_00002", "isError=true");
        assertThat(rpc("tools/call", Map.of("name", "dropTables"))).containsKey("error");
        assertThat(rpc("tools/call", Map.of())).containsKey("error");
    }

    @Test
    void aDatabaseFailureHidesItsCause() {
        jdbc.sql("ALTER TABLE products RENAME TO products_gone").update();
        try {
            assertThat(call("searchProducts", Map.of("query", "watches")))
                    .contains("SLHD_CATALOG_00003", "isError=true")
                    .doesNotContain("products", "SELECT");
            client.get().uri("/api/catalog/products").exchange()
                    .expectStatus().isEqualTo(503)
                    .expectBody().jsonPath("$.code").isEqualTo("SLHD_CATALOG_00003");
        } finally {
            jdbc.sql("ALTER TABLE products_gone RENAME TO products").update();
        }
    }

    private void assertTotal(String filter, int total) {
        String[] parts = filter.split("&");
        client.get().uri(builder -> {
            builder.path("/api/catalog/products");
            for (String part : parts) {
                String[] pair = part.split("=", 2);
                builder.queryParam(pair[0], "{" + pair[0] + "}");
            }
            return builder.build((Object[]) java.util.Arrays.stream(parts).map(p -> p.split("=", 2)[1]).toArray());
        }).exchange().expectStatus().isOk().expectBody().jsonPath("$.totalElements").isEqualTo(total);
    }

    private String call(String tool, Map<String, Object> arguments) {
        return rpc("tools/call", Map.of("name", tool, "arguments", arguments)).get("result").toString();
    }

    private Map<String, Object> rpc(String method, Map<String, Object> params) {
        return client.post().uri("/mcp").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("jsonrpc", "2.0", "id", "1", "method", method, "params", params))
                .exchange().expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() { })
                .returnResult().getResponseBody();
    }
}
