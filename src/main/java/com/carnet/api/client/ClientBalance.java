package com.carnet.api.client;

public record ClientBalance(Client client, Long balance, Long lastOperationAt) {}