package com.carnet.api.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

record ClientRequest(
        @NotNull UUID id,
        @NotBlank @Size(min = 2, max = 60) String name,
        // Même règle que le téléphone : 9 chiffres, préfixes 70, 71, 76, 77, 78
        @Pattern(regexp = "^(70|71|76|77|78)\\d{7}$", message = "Numéro invalide") String phone,
        @Size(max = 100) String address,
        @Size(max = 200) String note) {}

record ClientResponse(UUID id, String name, String phone, String address, String note,
                      long balance, Long lastOperationAt, long createdAt, long updatedAt) {
    static ClientResponse from(ClientBalance cb) {
        Client c = cb.client();
        return new ClientResponse(c.getId(), c.getName(), c.getPhone(), c.getAddress(), c.getNote(),
                cb.balance(), cb.lastOperationAt(), c.getCreatedAt(), c.getUpdatedAt());
    }
}

@RestController
@RequestMapping("/api/clients")
public class ClientController {


    private final ClientRepository clients;

    public ClientController(ClientRepository clients) {
        this.clients = clients;
    }

    @GetMapping
    public List<ClientResponse> list(@AuthenticationPrincipal UUID userId) {
        return clients.findAllWithBalance(userId).stream()
                .sorted(Comparator.comparing(ClientBalance::balance).reversed())
                .map(ClientResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ClientResponse get(@AuthenticationPrincipal UUID userId,@PathVariable UUID id) {
        return clients.findWithBalance(id,userId)
                .map(ClientResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client introuvable"));
    }

    @PostMapping
@ResponseStatus(HttpStatus.CREATED)
@Transactional
public ClientResponse create(@AuthenticationPrincipal UUID userId, @Valid @RequestBody ClientRequest req) {
    if (clients.existsById(req.id())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Client déjà existant");
    }
    long now = System.currentTimeMillis();
    Client c = new Client();
    c.setId(req.id());
    c.setUserId(userId);
    c.setName(req.name().trim());
    c.setPhone(req.phone());
    c.setAddress(req.address());
    c.setNote(req.note());
    c.setCreatedAt(now);
    c.setUpdatedAt(now);
    clients.save(c);
    return get(c.getId(), userId);
}

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@AuthenticationPrincipal UUID userId,@PathVariable UUID id) {
        Client c = clients.findByIdAndUserIdAndDeletedAtIsNull(id,userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client introuvable"));
        long now = System.currentTimeMillis();
        c.setDeletedAt(now);   // suppression logique, comme sur le téléphone
        c.setUpdatedAt(now);
    }
}