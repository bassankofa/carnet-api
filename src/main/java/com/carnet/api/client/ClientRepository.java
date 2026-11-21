package com.carnet.api.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    @Query("""
        SELECT new com.carnet.api.client.ClientBalance(
            c,
            COALESCE(SUM(CASE WHEN o.type = com.carnet.api.operation.OperationType.DEBT THEN o.amount
                              WHEN o.type = com.carnet.api.operation.OperationType.PAYMENT THEN -o.amount
                         END), 0),
            MAX(o.createdAt))
        FROM Client c
        LEFT JOIN Operation o ON o.clientId = c.id AND o.deletedAt IS NULL
        WHERE c.userId = :userId AND c.deletedAt IS NULL
        GROUP BY c
        """)
    List<ClientBalance> findAllWithBalance(@Param("userId") UUID userId);

    @Query("""
        SELECT new com.carnet.api.client.ClientBalance(
            c,
            COALESCE(SUM(CASE WHEN o.type = com.carnet.api.operation.OperationType.DEBT THEN o.amount
                              WHEN o.type = com.carnet.api.operation.OperationType.PAYMENT THEN -o.amount
                         END), 0),
            MAX(o.createdAt))
        FROM Client c
        LEFT JOIN Operation o ON o.clientId = c.id AND o.deletedAt IS NULL
        WHERE c.id = :id AND c.userId = :userId AND c.deletedAt IS NULL
        GROUP BY c
        """)
    Optional<ClientBalance> findWithBalance(@Param("id") UUID id, @Param("userId") UUID userId);

    Optional<Client> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<Client> findByUserIdAndUpdatedAtGreaterThanEqual(UUID userId, long since);
}