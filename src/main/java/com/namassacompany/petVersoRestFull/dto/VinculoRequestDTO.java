package com.namassacompany.petVersoRestFull.dto;

import jakarta.validation.constraints.NotBlank;

public record VinculoRequestDTO(
       @NotBlank String codigoVinculo
) {
}
