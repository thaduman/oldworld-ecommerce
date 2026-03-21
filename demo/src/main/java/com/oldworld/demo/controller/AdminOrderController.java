package com.oldworld.demo.controller;

import com.oldworld.demo.model.Order;
import com.oldworld.demo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * PAKET: com.oldworld.demo.controller
 * DOSYA ADI: AdminOrderController.java
 *
 * Bu controller ADMIN ve SELLER rollerine özel endpoint'ler sunar.
 * SecurityConfig'de @EnableMethodSecurity aktif edilmelidir (aşağıda açıklandı).
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "null"})
public class AdminOrderController {

    private final OrderRepository orderRepository;

    /**
     * TÜM SİPARİŞLERİ LİSTELE
     * GET /api/admin/orders
     * Yetki: ADMIN veya SELLER
     *
     * Frontend kullanımı:
     *   fetch('/api/admin/orders', { headers: { Authorization: 'Bearer ' + token } })
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
        return ResponseEntity.ok(orders);
    }

    /**
     * TEK SİPARİŞ DETAYI
     * GET /api/admin/orders/{id}
     * Yetki: ADMIN veya SELLER
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * SİPARİŞ DURUMU GÜNCELLE
     * PATCH /api/admin/orders/{id}/status
     * Yetki: ADMIN veya SELLER
     *
     * Request body: { "status": "SHIPPED" }
     * Geçerli durumlar: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String newStatus = body.get("status");

        // Geçerli durum kontrolü
        List<String> validStatuses = List.of(
            "PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED", "COMPLETED"
        );
        if (newStatus == null || !validStatuses.contains(newStatus.toUpperCase())) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "Geçersiz durum. Geçerli değerler: " + validStatuses)
            );
        }

        return orderRepository.findById(id).map(order -> {
            order.setStatus(newStatus.toUpperCase());
            orderRepository.save(order);
            return ResponseEntity.ok(Map.of(
                "message", "Sipariş #" + id + " durumu '" + newStatus + "' olarak güncellendi.",
                "orderId", id,
                "newStatus", newStatus.toUpperCase()
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * SİPARİŞ İSTATİSTİKLERİ
     * GET /api/admin/orders/stats
     * Yetki: ADMIN veya SELLER
     *
     * Dashboard için: toplam sipariş, toplam ciro, durum bazlı sayılar
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<?> getOrderStats() {
        List<Order> all = orderRepository.findAll();

        long total = all.size();
        double totalRevenue = all.stream()
                .filter(o -> !"CANCELLED".equals(o.getStatus()))
                .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0)
                .sum();
        long completed  = all.stream().filter(o -> "COMPLETED".equals(o.getStatus())).count();
        long pending    = all.stream().filter(o -> "PENDING".equals(o.getStatus())).count();
        long cancelled  = all.stream().filter(o -> "CANCELLED".equals(o.getStatus())).count();
        long shipped    = all.stream().filter(o -> "SHIPPED".equals(o.getStatus())).count();

        return ResponseEntity.ok(Map.of(
            "totalOrders",   total,
            "totalRevenue",  totalRevenue,
            "completed",     completed,
            "pending",       pending,
            "cancelled",     cancelled,
            "shipped",       shipped
        ));
    }
}