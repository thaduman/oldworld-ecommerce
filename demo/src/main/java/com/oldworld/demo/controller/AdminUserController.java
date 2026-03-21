package com.oldworld.demo.controller;

import com.oldworld.demo.model.Role;
import com.oldworld.demo.model.User;
import com.oldworld.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PAKET: com.oldworld.demo.controller
 * DOSYA ADI: AdminUserController.java
 *
 * ADMIN'e özel kullanıcı yönetimi endpoint'leri.
 * Şifreler hiçbir zaman response'a eklenmez.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "null"})
public class AdminUserController {

    private final UserRepository userRepository;

    /**
     * TÜM KULLANICILARI LİSTELE
     * GET /api/admin/users
     * Yetki: Sadece ADMIN
     *
     * Şifreler temizlenerek döner.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll()
                .stream()
                .map(this::toSafeMap)  // Şifreyi asla gönderme
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    /**
     * TEK KULLANICI DETAYI
     * GET /api/admin/users/{id}
     * Yetki: Sadece ADMIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(toSafeMap(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * KULLANICI ROLÜNÜ GÜNCELLE
     * PATCH /api/admin/users/{id}/role
     * Yetki: Sadece ADMIN
     *
     * Request body: { "role": "SELLER" }
     * Geçerli roller: USER, SELLER, ADMIN
     *
     * Kullanım senaryosu: Bir kullanıcıya satıcı yetkisi vermek
     */
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String newRoleStr = body.get("role");

        // Geçerli rol kontrolü
        Role newRole;
        try {
            newRole = Role.valueOf(newRoleStr.toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "Geçersiz rol. Geçerli değerler: USER, SELLER, ADMIN")
            );
        }

        return userRepository.findById(id).map(user -> {
            user.setRole(newRole);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of(
                "message", user.getEmail() + " kullanıcısının rolü '" + newRole + "' olarak güncellendi.",
                "userId", id,
                "newRole", newRole.name()
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * KULLANICI SİL
     * DELETE /api/admin/users/{id}
     * Yetki: Sadece ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            return ResponseEntity.ok(Map.of(
                "message", id + " ID'li kullanıcı silindi."
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * KULLANICI İSTATİSTİKLERİ
     * GET /api/admin/users/stats
     * Yetki: Sadece ADMIN
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserStats() {
        List<User> all = userRepository.findAll();
        long totalUsers   = all.stream().filter(u -> Role.USER.equals(u.getRole())).count();
        long totalSellers = all.stream().filter(u -> Role.SELLER.equals(u.getRole())).count();
        long totalAdmins  = all.stream().filter(u -> Role.ADMIN.equals(u.getRole())).count();

        return ResponseEntity.ok(Map.of(
            "total",   all.size(),
            "users",   totalUsers,
            "sellers", totalSellers,
            "admins",  totalAdmins
        ));
    }

    // ---- YARDIMCI: Şifreyi response'dan temizler ----
    private Map<String, Object> toSafeMap(User u) {
        return Map.of(
            "id",          u.getId(),
            "email",       u.getEmail() != null ? u.getEmail() : "",
            "displayName", u.getDisplayName() != null ? u.getDisplayName() : "",
            "phone",       u.getPhone() != null ? u.getPhone() : "",
            "role",        u.getRole() != null ? u.getRole().name() : "USER"
        );
    }
}