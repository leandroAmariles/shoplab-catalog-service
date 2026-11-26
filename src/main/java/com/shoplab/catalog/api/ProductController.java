package com.shoplab.catalog.api;

import com.shoplab.catalog.api.dto.*;
import com.shoplab.catalog.application.GetProductDetailUseCase;
import com.shoplab.catalog.application.GetProductStockUseCase;
import com.shoplab.catalog.application.GetProductsPageUseCase;
import com.shoplab.catalog.domain.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/products")
public class ProductController {

    private final GetProductsPageUseCase getProductsPageUseCase;
    private final GetProductDetailUseCase getProductDetailUseCase;
    private final GetProductStockUseCase getProductStockUseCase;

    public ProductController(
            GetProductsPageUseCase getProductsPageUseCase,
            GetProductDetailUseCase getProductDetailUseCase,
            GetProductStockUseCase getProductStockUseCase
    ) {
        this.getProductsPageUseCase = getProductsPageUseCase;
        this.getProductDetailUseCase = getProductDetailUseCase;
        this.getProductStockUseCase = getProductStockUseCase;
    }

    @GetMapping
    public Mono<ResponseEntity<ProductPageDto>> getProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "inStock", required = false) Boolean inStock
    ) {
        return Mono.fromCallable(() -> {
            var productPage = getProductsPageUseCase.execute(page, size, category, query, inStock);
            List<ProductSummaryDto> dtos = productPage.content().stream()
                    .map(this::toSummaryDto)
                    .toList();

            return ResponseEntity.ok(new ProductPageDto(
                    dtos,
                    productPage.page(),
                    productPage.size(),
                    productPage.totalElements(),
                    productPage.totalPages(),
                    productPage.last()
            ));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ProductDetailDto>> getProductById(@PathVariable("id") String id) {
        return Mono.fromCallable(() -> {
            Product product = getProductDetailUseCase.execute(id);
            return ResponseEntity.ok(toDetailDto(product));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/{id}/stock")
    public Mono<ResponseEntity<ProductStockDto>> getProductStock(@PathVariable("id") String id) {
        return Mono.fromCallable(() -> {
            var stockInfo = getProductStockUseCase.execute(id);
            return ResponseEntity.ok(new ProductStockDto(
                    stockInfo.productId(),
                    stockInfo.referencePrice(),
                    stockInfo.stock(),
                    stockInfo.available(),
                    stockInfo.updatedAt()
            ));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private ProductSummaryDto toSummaryDto(Product p) {
        ProductDimensionsDto dims = p.dimensions() != null
                ? new ProductDimensionsDto(
                        p.dimensions().lengthCm(), p.dimensions().heightCm(), p.dimensions().widthCm())
                : null;
        return new ProductSummaryDto(
                p.productId(),
                p.categoryName(),
                p.categoryNameEn(),
                p.referencePrice(),
                p.stock(),
                p.weightG(),
                dims
        );
    }

    private ProductDetailDto toDetailDto(Product p) {
        ProductDimensionsDto dims = p.dimensions() != null
                ? new ProductDimensionsDto(
                        p.dimensions().lengthCm(), p.dimensions().heightCm(), p.dimensions().widthCm())
                : null;
        return new ProductDetailDto(
                p.productId(),
                p.categoryName(),
                p.categoryNameEn(),
                p.nameLength(),
                p.descriptionLength(),
                p.photosCount(),
                p.weightG(),
                dims,
                p.referencePrice(),
                p.stock(),
                p.isAvailable()
        );
    }
}
