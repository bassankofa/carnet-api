package com.carnet.api.auth;

import com.carnet.api.user.User;
import com.carnet.api.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

record RegisterRequest(
        @NotBlank @Size(min = 2, max = 60) String name,
        @Pattern(regexp = "^(70|71|76|77|78)\\d{7}$", message = "Numéro invalide") String phone,
        @NotBlank @Size(min = 8, max = 72) String password) {}  // 72 : limite de BCrypt

record LoginRequest(@NotBlank String phone, @NotBlank String password) {}

record AuthResponse(String token, String name) {}

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        if (users.existsByPhone(req.phone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce numéro est déjà utilisé");
        }
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setPhone(req.phone());
        u.setName(req.name().trim());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setCreatedAt(System.currentTimeMillis());
        users.save(u);
        return new AuthResponse(jwt.issue(u.getId()), u.getName());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        // Même message si le numéro est inconnu ou si le mot de passe est faux :
        // on ne révèle pas quels numéros ont un compte
        User u = users.findByPhone(req.phone())
                .filter(x -> encoder.matches(req.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Numéro ou mot de passe incorrect"));
        return new AuthResponse(jwt.issue(u.getId()), u.getName());
    }
}