package com.pos.seguridad;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", auth.getName());
        
        // Obtener el rol, asumiendo que solo hay uno por simplificar
        String rol = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .findFirst()
                .orElse("");
                
        userInfo.put("rol", rol);
        
        return ResponseEntity.ok(userInfo);
    }
}
