package cl.duoc.tienda.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "El token de renovación es obligatorio")
        String refreshToken
) {
}
