package com.project.ecommerce.catalog.controller.admin;
// ... imports ...
@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {
    @Autowired private CategoryService categoryService;

    @GetMapping // READ
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping // CREATE
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        Category newCategory = categoryService.createCategory(request);
        return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
    }

    @PutMapping("/{id}") // UPDATE
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id, @Valid @RequestBody CategoryCreateRequest request) {
        Category updatedCategory = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}") // DELETE
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}