package com.namassacompany.petVersoRestFull.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioDadosRequestDTO(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank  @Size(min = 6) String telefone
) {
}
