package com.carnet.api.sync;

import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService sync;

    public SyncController(SyncService sync) {
        this.sync = sync;
    }

    @GetMapping
    public SyncPullResponse pull(@AuthenticationPrincipal UUID userId,@RequestParam(defaultValue = "0") long since) {
        return sync.pull(userId,since);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public SyncPushResponse push(@AuthenticationPrincipal UUID userId,@Valid @RequestBody SyncPushRequest req) {
        return sync.push(userId,req);
    }
}