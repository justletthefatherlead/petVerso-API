package com.namassacompany.petVersoRestFull.dto;

import jakarta.validation.constraints.Size;

public record RecusarSolicitacaoResponseDTO(
        @Size(max = 255) String motivo
) {}
