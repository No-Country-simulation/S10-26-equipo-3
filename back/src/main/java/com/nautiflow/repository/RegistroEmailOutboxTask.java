package com.nautiflow.repository;

public record RegistroEmailOutboxTask(long id, long idLanchero, String correo, int intento) {
}
