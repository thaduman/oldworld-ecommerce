package com.oldworld.demo.util;

import com.oldworld.demo.model.Product;
import com.oldworld.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // Eğer veritabanında hiç ürün yoksa örnek verileri ekle
        if (productRepository.count() == 0) {
            
            productRepository.save(new Product(
                null, 
                "Antika Anadolu Halısı", 
                "19. yüzyıldan kalma, kök boyalı el dokuması nadide bir parça.", 
                new BigDecimal("8500.00"), 
                "Halı", 
                "https://images.unsplash.com/photo-1576016773942-3175772b2799", // Örnek resim
                3, 
                "admin@test.com"
            ));

            productRepository.save(new Product(
                null, 
                "Osmanlı Dönemi Bakır İbrik", 
                "El işçiliği ile dövülmüş, koleksiyonluk antika bakır ibrik.", 
                new BigDecimal("2450.00"), 
                "Mutfak", 
                "https://images.unsplash.com/photo-1584622650111-993a426fbf0a", // Örnek resim
                1, 
                "admin@test.com"
            ));

            System.out.println(">> Veritabanına örnek ürünler başarıyla eklendi.");
        }
    }
}