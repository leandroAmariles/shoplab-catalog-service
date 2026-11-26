package com.shoplab.catalog.infrastructure.persistence;

import com.shoplab.catalog.domain.CategoryRepository;
import com.shoplab.catalog.domain.ProductCategory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PostgresCategoryRepository implements CategoryRepository {

    private final JdbcClient jdbcClient;

    public PostgresCategoryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<ProductCategory> findAll() {
        return jdbcClient.sql("SELECT name, name_en FROM product_categories ORDER BY name ASC")
                .query((rs, rowNum) -> new ProductCategory(
                        rs.getString("name"),
                        rs.getString("name_en")
                ))
                .list();
    }

    @Override
    public Optional<ProductCategory> findByName(String name) {
        return jdbcClient.sql("SELECT name, name_en FROM product_categories WHERE name = :name")
                .param("name", name)
                .query((rs, rowNum) -> new ProductCategory(
                        rs.getString("name"),
                        rs.getString("name_en")
                ))
                .optional();
    }
}
