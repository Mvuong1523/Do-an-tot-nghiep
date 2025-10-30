package com.project.ecommerce.catalog.controller.admin;
// ... imports ...
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {
    @Autowired private ProductService productService;

    @PostMapping // CREATE
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        Product newProduct = productService.createProduct(request);
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }

    @PutMapping("/{id}") // UPDATE
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id, @Valid @RequestBody ProductCreateRequest request) {
        Product updatedProduct = productService.updateProduct(id, request);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}") // DELETE
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}