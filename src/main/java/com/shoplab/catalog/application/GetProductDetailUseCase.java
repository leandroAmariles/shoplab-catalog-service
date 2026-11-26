package com.shoplab.catalog.application;

import com.shoplab.catalog.domain.Product;
import com.shoplab.catalog.domain.ProductRepository;
import com.shoplab.catalog.shared.exception.BusinessException;
import com.shoplab.catalog.shared.exception.ResponseCodeEnum;
import org.springframework.stereotype.Service;

@Service
public class GetProductDetailUseCase {

    private final ProductRepository productRepository;

    public GetProductDetailUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product execute(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new BusinessException(ResponseCodeEnum.INVALID_PARAMETERS, "Product ID cannot be empty");
        }

        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ResponseCodeEnum.PRODUCT_NOT_FOUND,
                        "Producto con ID " + productId + " no encontrado"));
    }
}
