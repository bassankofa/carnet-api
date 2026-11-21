package com.carnet.api.sync;

import com.carnet.api.operation.OperationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;


 record ClientChange(
        @NotNull UUID id,
        @NotBlank @Size(max = 60) String name,
        @Size(max = 20) String phone,
        @Size(max = 100) String address,
        @Size(max = 200) String note,
        long createdAt,
        long updatedAt,
        Long deletedAt) {}

 record OperationChange(
        @NotNull UUID id,
        @NotNull UUID clientId,
        @NotNull OperationType type,
        @Positive long amount,
        @Size(max = 80) String label,
        Long dueDate,
        long createdAt,
        long updatedAt,
        Long deletedAt) {}

 record SyncPushRequest(@Valid List<ClientChange> clients, @Valid List<OperationChange> operations) {}

 record SyncPullResponse(long serverTime, List<ClientChange> clients, List<OperationChange> operations) {}

 record SyncPushResponse(long serverTime) {}