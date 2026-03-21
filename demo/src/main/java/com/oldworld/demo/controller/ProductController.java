package com.oldworld.demo.controller;

import com.oldworld.demo.model.Product;
import com.oldworld.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // React uygulamanın erişimine izin veriyoruz
public class ProductController {

    private final ProductRepository productRepository;

    // --- TÜM ÜRÜNLERİ LİSTELE ---
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // --- TEK ÜRÜN DETAYI ---
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- YENİ ÜRÜN EKLE (Token Gerektirir) ---
    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody Product product) {
        try {
            Product savedProduct = productRepository.save(product);
            // Frontend'e başarılı kayıt sonrası JSON objesi dönüyoruz
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Hata: " + e.getMessage()));
        }
    }

    // --- ÜRÜN SİL (Token Gerektirir) ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        return productRepository.findById(id).map(p -> {
            productRepository.delete(p);
            // Frontend tarafında axios'un başarılı sayması için JSON yanıtı daha sağlıklı olur
            return ResponseEntity.ok(Map.of("message", id + " ID'li ürün başarıyla silindi."));
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- ÜRÜN GÜNCELLE (Token Gerektirir) ---
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product details) {
        return productRepository.findById(id).map(p -> {
            p.setName(details.getName());
            p.setDescription(details.getDescription());
            p.setPrice(details.getPrice());
            p.setImageUrl(details.getImageUrl());
            p.setCategory(details.getCategory());
            return ResponseEntity.ok(productRepository.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }
}