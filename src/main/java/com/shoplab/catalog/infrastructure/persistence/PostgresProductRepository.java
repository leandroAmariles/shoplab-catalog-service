package com.shoplab.catalog.infrastructure.persistence;

import com.shoplab.catalog.domain.Product;
import com.shoplab.catalog.domain.ProductDimensions;
import com.shoplab.catalog.domain.ProductRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Reads of the catalog. Every value that comes from a request travels as a bind parameter. */
@Repository
public class PostgresProductRepository implements ProductRepository {

    private static final String COLUMNS = """
            SELECT p.product_id, p.category_name, c.name_en, p.name_length, p.description_length,
                   p.photos_count, p.weight_g, p.length_cm, p.height_cm, p.width_cm,
                   p.reference_price, p.stock, p.updated_at
            """;

    private static final String FROM = """
            FROM products p
            LEFT JOIN product_categories c ON p.category_name = c.name
            """;

    /** Longer searches add conditions without narrowing anything useful. */
    private static final int MAX_QUERY_WORDS = 8;

    private final JdbcClient jdbcClient;

    public PostgresProductRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<Product> findProducts(long offset, int limit, String category, String query, Boolean inStock) {
        Filter filter = Filter.of(category, query, inStock);
        List<Object> params = new ArrayList<>(filter.params());
        params.add(limit);
        params.add(offset);
        return jdbcClient
                .sql(COLUMNS + FROM + filter.where() + " ORDER BY p.product_id ASC LIMIT ? OFFSET ?")
                .params(params)
                .query((rs, rowNum) -> toProduct(rs))
                .list();
    }

    @Override
    public long countProducts(String category, String query, Boolean inStock) {
        Filter filter = Filter.of(category, query, inStock);
        return jdbcClient
                .sql("SELECT count(*) " + FROM + filter.where())
                .params(filter.params())
                .query(Long.class)
                .single();
    }

    @Override
    public Optional<Product> findById(String productId) {
        return jdbcClient
                .sql(COLUMNS + FROM + " WHERE p.product_id = ?")
                .param(productId)
                .query((rs, rowNum) -> toProduct(rs))
                .optional();
    }

    private static Product toProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getString("product_id"),
                rs.getString("category_name"),
                rs.getString("name_en"),
                rs.getObject("name_length", Integer.class),
                rs.getObject("description_length", Integer.class),
                rs.getObject("photos_count", Integer.class),
                rs.getObject("weight_g", Integer.class),
                new ProductDimensions(
                        rs.getObject("length_cm", Integer.class),
                        rs.getObject("height_cm", Integer.class),
                        rs.getObject("width_cm", Integer.class)),
                rs.getBigDecimal("reference_price"),
                rs.getInt("stock"),
                rs.getTimestamp("updated_at").toInstant());
    }

    private record Filter(String where, List<Object> params) {

        /** Olist has no product titles: a word matches the product id or the category in Portuguese or English. */
        static Filter of(String category, String query, Boolean inStock) {
            StringBuilder where = new StringBuilder(" WHERE 1=1");
            List<Object> params = new ArrayList<>();
            if (category != null && !category.isBlank()) {
                where.append(" AND (p.category_name = ? OR c.name_en = ?)");
                params.add(category);
                params.add(category);
            }
            if (query != null && !query.isBlank()) {
                String[] words = query.trim().split("\\s+");
                for (int i = 0; i < Math.min(words.length, MAX_QUERY_WORDS); i++) {
                    where.append(" AND (p.product_id ILIKE ? OR p.category_name ILIKE ? OR c.name_en ILIKE ?)");
                    String pattern = "%" + escapeLike(words[i]) + "%";
                    params.add(pattern);
                    params.add(pattern);
                    params.add(pattern);
                }
            }
            if (Boolean.TRUE.equals(inStock)) {
                where.append(" AND p.stock > 0");
            }
            return new Filter(where.toString(), params);
        }

        private static String escapeLike(String word) {
            return word.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        }
    }
}
