package com.carnet.api.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor 
public class User {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 9)
    private String phone;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private long createdAt;
} 
