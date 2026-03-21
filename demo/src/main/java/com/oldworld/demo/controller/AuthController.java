package com.oldworld.demo.controller;

import com.oldworld.demo.dto.AuthRequest;
import com.oldworld.demo.model.Role;
import com.oldworld.demo.model.User;
import com.oldworld.demo.repository.UserRepository;
import com.oldworld.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository repo;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            if (repo.existsByEmail(user.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Hata: Email zaten kayıtlı"));
            }

            user.setPassword(encoder.encode(user.getPassword()));
            
            // Eğer formdan rol gelmemişse varsayılan olarak USER ata
            if (user.getRole() == null) {
                user.setRole(Role.USER);
            }

            repo.save(user);
            return ResponseEntity.ok(Map.of("message", "Kayıt başarılı"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Hata: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        try {
            // 1. Kimlik doğrulama yap
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );
            
            // 2. Kullanıcıyı veritabanından bul (Rol bilgisini almak için şart)
            User user = repo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
            
            // 3. Token üret
            String token = jwtUtil.generateToken(req.getEmail());
            
            // 4. Frontend'in beklediği tam JSON yapısını oluştur
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            
            // User objesinin içine 'role' bilgisini de ekliyoruz
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("username", user.getEmail());
            userInfo.put("role", user.getRole().name()); // SELLER veya USER döner
            
            response.put("user", userInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Hata: Email veya şifre yanlış"));
        }
    }
}