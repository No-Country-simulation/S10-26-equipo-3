package com.nautiflow.repository;

public record RegistroEmailOutboxPayload(String json, String idempotencyKey) {
}
