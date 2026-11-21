package com.carnet.api.client;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "clients")
@Getter @Setter @NoArgsConstructor
public class Client {
    @Id
    private UUID id;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String address;

    @Column(length = 200)
    private String note;

    @Column(name = "created_at", nullable = false)
    private long createdAt;

    @Column(name = "updated_at", nullable = false)
    private long updatedAt;

    @Column(name = "deleted_at")
    private Long deletedAt;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
}