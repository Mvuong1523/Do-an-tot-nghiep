package com.doan.WEB_TMDT.module.product.controller;

import com.doan.WEB_TMDT.module.product.dto.CategoryDTO;
import com.doan.WEB_TMDT.module.product.dto.CreateCategoryRequest;
import com.doan.WEB_TMDT.module.product.dto.UpdateCategoryRequest;
import com.doan.WEB_TMDT.module.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public CategoryDTO create(@RequestBody CreateCategoryRequest req) {
        return categoryService.create(req);
    }

    @PutMapping("/{id}")
    public CategoryDTO update(@PathVariable Long id, @RequestBody UpdateCategoryRequest req) {
        return categoryService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }

    @GetMapping("/{id}")
    public CategoryDTO getById(@PathVariable Long id) {
        return categoryService.getById(id);
    }

    @GetMapping
    public List<CategoryDTO> getAll() {
        return categoryService.getAll();
    }
}
