package com.shoplab.catalog.application;

import com.shoplab.catalog.domain.Product;
import com.shoplab.catalog.domain.ProductRepository;
import com.shoplab.catalog.shared.exception.BusinessException;
import com.shoplab.catalog.shared.exception.ResponseCodeEnum;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class GetProductStockUseCase {

    private final ProductRepository productRepository;

    public GetProductStockUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public StockInfo execute(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ResponseCodeEnum.PRODUCT_NOT_FOUND,
                        "Producto con ID " + productId + " no encontrado"));

        return new StockInfo(
                product.productId(),
                product.referencePrice(),
                product.stock(),
                product.isAvailable(),
                product.updatedAt()
        );
    }

    public record StockInfo(
            String productId,
            BigDecimal referencePrice,
            int stock,
            boolean available,
            Instant updatedAt
    ) {}
}
