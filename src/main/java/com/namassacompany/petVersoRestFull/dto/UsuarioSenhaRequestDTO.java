package com.namassacompany.petVersoRestFull.dto;

import jakarta.validation.constraints.NotBlank;

public record UsuarioSenhaRequestDTO(
        @NotBlank String senhaAtual,
        @NotBlank String novaSenha
) {
}
