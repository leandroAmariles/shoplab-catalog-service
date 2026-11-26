package com.shoplab.catalog.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.shoplab.catalog.application.GetCategoriesUseCase;
import com.shoplab.catalog.application.GetProductDetailUseCase;
import com.shoplab.catalog.application.GetProductStockUseCase;
import com.shoplab.catalog.application.GetProductsPageUseCase;
import com.shoplab.catalog.application.ProductPage;
import com.shoplab.catalog.domain.Product;
import com.shoplab.catalog.domain.ProductCategory;
import com.shoplab.catalog.domain.ProductDimensions;
import com.shoplab.catalog.shared.exception.BusinessException;
import com.shoplab.catalog.shared.exception.GlobalExceptionHandler;
import com.shoplab.catalog.shared.exception.ResponseCodeEnum;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.reactive.server.WebTestClient;

/** Base class of the producer verification of the catalog HTTP API. */
public abstract class CatalogContractBase {

    @BeforeEach
    void setup() {
        Product product = new Product("a1", "relogios_presentes", "watches_gifts", 40, 287, 1, 200,
                new ProductDimensions(16, 10, 14), new BigDecimal("99.90"), 7,
                Instant.parse("2026-11-26T12:00:00Z"));

        GetProductsPageUseCase page = mock(GetProductsPageUseCase.class);
        GetProductDetailUseCase detail = mock(GetProductDetailUseCase.class);
        GetProductStockUseCase stock = mock(GetProductStockUseCase.class);
        GetCategoriesUseCase categories = mock(GetCategoriesUseCase.class);

        when(page.execute(0, 1, null, null, null)).thenReturn(ProductPage.of(List.of(product), 0, 1, 2));
        when(detail.execute("a1")).thenReturn(product);
        when(detail.execute("missing")).thenThrow(new BusinessException(ResponseCodeEnum.PRODUCT_NOT_FOUND));
        when(stock.execute("a1")).thenReturn(new GetProductStockUseCase.StockInfo(
                "a1", new BigDecimal("99.90"), 7, true, product.updatedAt()));
        when(categories.execute()).thenReturn(List.of(new ProductCategory("relogios_presentes", "watches_gifts")));

        RestAssuredWebTestClient.webTestClient(WebTestClient
                .bindToController(new ProductController(page, detail, stock), new CategoryController(categories))
                .controllerAdvice(new GlobalExceptionHandler())
                .build());
    }
}
