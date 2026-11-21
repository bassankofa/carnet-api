package com.carnet.api.operation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OperationRepository extends JpaRepository<Operation, UUID> {
    List<Operation> findByUserIdAndClientIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId, UUID clientId);
    Optional<Operation> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);
    List<Operation> findByUserIdAndUpdatedAtGreaterThanEqual(UUID userId, long since);
}