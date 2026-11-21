package com.carnet.api.operation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "operations")
@Getter @Setter @NoArgsConstructor
public class Operation {
    @Id
    private UUID id;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OperationType type;

    @Column(nullable = false)
    private long amount;

    @Column(length = 80)
    private String label;

    @Column(name = "due_date")
    private Long dueDate;

    @Column(name = "created_at", nullable = false)
    private long createdAt;

    @Column(name = "updated_at", nullable = false)
    private long updatedAt;

    @Column(name = "deleted_at")
    private Long deletedAt;

    @Column(name = "user_id", nullable = false)
    private UUID userId;
}