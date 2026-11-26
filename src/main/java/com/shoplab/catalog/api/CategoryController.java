package com.shoplab.catalog.api;

import com.shoplab.catalog.api.dto.CategoryDto;
import com.shoplab.catalog.application.GetCategoriesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/categories")
public class CategoryController {

    private final GetCategoriesUseCase getCategoriesUseCase;

    public CategoryController(GetCategoriesUseCase getCategoriesUseCase) {
        this.getCategoriesUseCase = getCategoriesUseCase;
    }

    @GetMapping
    public Mono<ResponseEntity<List<CategoryDto>>> getCategories() {
        return Mono.fromCallable(() -> {
            List<CategoryDto> dtos = getCategoriesUseCase.execute().stream()
                    .map(cat -> new CategoryDto(cat.name(), cat.nameEn()))
                    .toList();
            return ResponseEntity.ok(dtos);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
