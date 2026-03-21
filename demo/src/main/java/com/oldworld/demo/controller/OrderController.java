package com.oldworld.demo.controller;

import com.oldworld.demo.model.Order;
import com.oldworld.demo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * PAKET: com.oldworld.demo.controller
 * DOSYA ADI: OrderController.java  (mevcut dosyanın yerini alır)
 *
 * DEĞİŞİKLİKLER:
 *   1. CORS origins güncellendi ("null" eklendi)
 *   2. Sipariş durumu güncelleme endpoint'i KALDIRILDI
 *      → AdminOrderController'a PATCH /api/admin/orders/{id}/status olarak taşındı
 *      → Böylece sadece admin/seller durumu değiştirebilir, kullanıcı kendi
 *        siparişini "COMPLETED" yapamaz
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "null"})
public class OrderController {

    private final OrderRepository orderRepository;

    /**
     * YENİ SİPARİŞ OLUŞTUR
     * POST /api/orders/place
     * Yetki: Giriş yapmış herhangi bir kullanıcı
     */
    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody Order order) {
        try {
            if (order.getItems() == null || order.getItems().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Hata: Koleksiyon seçilmeden sipariş verilemez.");
            }

            // Her item için sipariş referansını bağla
            order.getItems().forEach(item -> item.setOrder(order));

            order.setOrderDate(LocalDateTime.now());
            order.setStatus("PENDING");  // Artık COMPLETED değil, PENDING başlıyor

            Order savedOrder = orderRepository.save(order);
            return ResponseEntity.ok(savedOrder);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Sipariş işlenirken bir sorun oluştu: " + e.getMessage());
        }
    }

    /**
     * KULLANICINIn KENDİ SİPARİŞ GEÇMİŞİ
     * GET /api/orders/my-orders/{email}
     * Yetki: Giriş yapmış kullanıcı (kendi siparişlerini görür)
     */
    @GetMapping("/my-orders/{email}")
    public ResponseEntity<?> getMyOrders(@PathVariable String email) {
        try {
            List<Order> orders = orderRepository
                    .findByCustomerEmailOrderByOrderDateDesc(email);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Siparişleriniz yüklenirken bir hata oluştu.");
        }
    }
}