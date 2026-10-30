package com.nautiflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LancheroRegistroDTO(
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 201, message = "El nombre completo no puede superar 201 caracteres")
        String nombreCompleto,

        @NotBlank(message = "El documento de identidad es obligatorio")
        @Size(max = 50, message = "El documento de identidad no puede superar 50 caracteres")
        String documentoIdentidad,

        @NotBlank(message = "El número de WhatsApp es obligatorio")
        @Pattern(regexp = "^\\+?[0-9 ()-]{8,20}$", message = "El número de WhatsApp no es válido")
        String whatsapp,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Debe ser un correo válido")
        @Size(max = 100, message = "El correo no puede superar 100 caracteres")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String contrasena,

        @NotBlank(message = "El nombre de la embarcación es obligatorio")
        @Size(max = 100, message = "El nombre de la embarcación no puede superar 100 caracteres")
        String nombreEmbarcacion,

        @NotNull(message = "La capacidad de pasajeros es obligatoria")
        @jakarta.validation.constraints.Positive(message = "La capacidad debe ser mayor que cero")
        Integer capacidadPasajeros
) {
}
