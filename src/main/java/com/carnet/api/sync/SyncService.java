package com.carnet.api.sync;

import com.carnet.api.client.Client;
import com.carnet.api.client.ClientRepository;
import com.carnet.api.operation.Operation;
import com.carnet.api.operation.OperationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@Service
public class SyncService {

    private final ClientRepository clients;
    private final OperationRepository operations;

    public SyncService(ClientRepository clients, OperationRepository operations) {
        this.clients = clients;
        this.operations = operations;
    }

    @Transactional(readOnly = true)
    public SyncPullResponse pull(UUID userId, long since) {
        long serverTime = System.currentTimeMillis();
        return new SyncPullResponse(
                serverTime,
                clients.findByUserIdAndUpdatedAtGreaterThanEqual(userId, since).stream()
                        .map(SyncService::toChange).toList(),
                operations.findByUserIdAndUpdatedAtGreaterThanEqual(userId, since).stream()
                        .map(SyncService::toChange).toList()
        );
    }

    @Transactional
    public SyncPushResponse push(UUID userId, SyncPushRequest req) {
        req.clients().forEach(c -> applyClient(userId, c));
        req.operations().forEach(o -> applyOperation(userId, o));
        return new SyncPushResponse(System.currentTimeMillis());
    }

    private void applyClient(UUID userId, ClientChange in) {
        Client existing = clients.findById(in.id()).orElse(null);
        if (existing == null) {
            Client c = new Client();
            c.setUserId(userId);
            copy(in, c);
            clients.save(c);
            return;
        }
        ensureOwner(existing.getUserId(), userId);
        if (in.updatedAt() > existing.getUpdatedAt()) {
            copy(in, existing); // dernier écrit gagne
        }
    }

    private void applyOperation(UUID userId, OperationChange in) {
        // L'opération doit se rattacher à un client de ce même compte
        Client client = clients.findById(in.clientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client inconnu"));
        ensureOwner(client.getUserId(), userId);

        Operation existing = operations.findById(in.id()).orElse(null);
        if (existing == null) {
            Operation op = new Operation();
            op.setId(in.id());
            op.setUserId(userId);
            op.setClientId(in.clientId());
            op.setType(in.type());
            op.setAmount(in.amount());
            op.setLabel(in.label());
            op.setDueDate(in.dueDate());
            op.setCreatedAt(in.createdAt());
            op.setUpdatedAt(in.updatedAt());
            op.setDeletedAt(in.deletedAt());
            operations.save(op);
        } else {
            ensureOwner(existing.getUserId(), userId);
            if (in.deletedAt() != null && existing.getDeletedAt() == null) {
                existing.setDeletedAt(in.deletedAt()); // la suppression l'emporte
                existing.setUpdatedAt(in.updatedAt());
            }
        }
    }

    private static void ensureOwner(UUID owner, UUID userId) {
        if (!owner.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Donnée appartenant à un autre compte");
        }
    }

    private static void copy(ClientChange in, Client c) {
        c.setId(in.id());
        c.setName(in.name());
        c.setPhone(in.phone());
        c.setAddress(in.address());
        c.setNote(in.note());
        c.setCreatedAt(in.createdAt());
        c.setUpdatedAt(in.updatedAt());
        c.setDeletedAt(in.deletedAt());
    }

    private static ClientChange toChange(Client c) {
        return new ClientChange(c.getId(), c.getName(), c.getPhone(), c.getAddress(), c.getNote(),
                c.getCreatedAt(), c.getUpdatedAt(), c.getDeletedAt());
    }

    private static OperationChange toChange(Operation o) {
        return new OperationChange(o.getId(), o.getClientId(), o.getType(), o.getAmount(), o.getLabel(),
                o.getDueDate(), o.getCreatedAt(), o.getUpdatedAt(), o.getDeletedAt());
    }
}