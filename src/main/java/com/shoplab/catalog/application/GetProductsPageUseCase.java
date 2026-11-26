package com.shoplab.catalog.application;

import com.shoplab.catalog.domain.Product;
import com.shoplab.catalog.domain.ProductRepository;
import com.shoplab.catalog.shared.exception.BusinessException;
import com.shoplab.catalog.shared.exception.ResponseCodeEnum;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetProductsPageUseCase {

    private final ProductRepository productRepository;

    public GetProductsPageUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductPage execute(int page, int size, String category, String query, Boolean inStock) {
        if (page < 0 || size < 1 || size > 200) {
            throw new BusinessException(ResponseCodeEnum.INVALID_PARAMETERS,
                    "Parametros de paginacion invalidos: page >= 0, 1 <= size <= 200");
        }

        long offset = (long) page * size;
        List<Product> products = productRepository.findProducts(offset, size, category, query, inStock);
        long total = productRepository.countProducts(category, query, inStock);

        return ProductPage.of(products, page, size, total);
    }
}
