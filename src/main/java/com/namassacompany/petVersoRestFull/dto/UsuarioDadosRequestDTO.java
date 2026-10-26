package com.namassacompany.petVersoRestFull.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioDadosRequestDTO(
        @NotBlank String nome,
        @NotBlank  @Size(min = 6) String telefone
) {
}
