package com.carnet.api.operation;

import com.carnet.api.client.Client;
import com.carnet.api.client.ClientRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

record OperationRequest(
        @NotNull UUID id,
        @NotNull OperationType type,
        @NotNull @Positive @Max(100_000_000L) Long amount,
        @Size(max = 80) String label,
        Long dueDate,  // facultatif, réservé aux dettes
        Long date) {}  // facultatif : date de l'opération, maintenant par défaut

record OperationResponse(UUID id, UUID clientId, OperationType type, long amount, String label,
                         Long dueDate, long createdAt, long updatedAt) {
    static OperationResponse from(Operation o) {
        return new OperationResponse(o.getId(), o.getClientId(), o.getType(), o.getAmount(),
                o.getLabel(), o.getDueDate(), o.getCreatedAt(), o.getUpdatedAt());
    }
}

@RestController
@RequestMapping("/api/clients/{clientId}/operations")
public class OperationController {

    private final OperationRepository operations;
    private final ClientRepository clients;

    public OperationController(OperationRepository operations, ClientRepository clients) {
        this.operations = operations;
        this.clients = clients;
    }

   
     @GetMapping
    public List<OperationResponse> findByClientIdAndDeletedAtIsNullOrderByCreatedAtDesc(@AuthenticationPrincipal UUID userId,@PathVariable UUID clientId) {
        findClient(userId,clientId);
        return operations.findByUserIdAndClientIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId,clientId).stream()
                .map(OperationResponse::from)
                .toList();
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@AuthenticationPrincipal UUID userId,@PathVariable UUID clientId, @PathVariable UUID id) {

         Client client = findClient(userId,clientId);

         Operation op = operations.findByIdAndUserIdAndDeletedAtIsNull(id,userId)
            .filter(o -> o.getClientId().equals(clientId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opération introuvable"));

            long now = System.currentTimeMillis();
            op.setDeletedAt(now);   // suppression logique : la ligne reste en base
            op.setUpdatedAt(now);
            client.setUpdatedAt(now);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public OperationResponse create(@AuthenticationPrincipal UUID userId,@PathVariable UUID clientId, @Valid @RequestBody OperationRequest req) {
        Client client = findClient(userId,clientId);

        // Si le téléphone renvoie la même opération (coupure réseau), on ne crée pas de doublon
        if (operations.existsById(req.id())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Opération déjà enregistrée");
        }

        long now = System.currentTimeMillis();
        long date = req.date() != null ? req.date() : now;
        if (date > now) {
            throw badRequest("La date de l'opération ne peut pas être dans le futur");
        }
        if (req.type() == OperationType.PAYMENT && req.dueDate() != null) {
            throw badRequest("Un paiement ne peut pas avoir d'échéance");
        }

        Operation op = new Operation();
        op.setId(req.id());
        op.setClientId(clientId);
        op.setType(req.type());
        op.setAmount(req.amount());
        op.setLabel(req.label() == null || req.label().isBlank() ? null : req.label().trim());
        op.setDueDate(req.dueDate());
        op.setCreatedAt(date);
        op.setUpdatedAt(now);
        
        op.setUserId(userId);
        operations.save(op);

        client.setUpdatedAt(now); // le client a changé : utile pour la synchro
        return OperationResponse.from(op);
    }

    private Client findClient(@AuthenticationPrincipal UUID userId,UUID clientId) {
        return clients.findByIdAndUserIdAndDeletedAtIsNull(userId,clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client introuvable"));
    }
    
    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}