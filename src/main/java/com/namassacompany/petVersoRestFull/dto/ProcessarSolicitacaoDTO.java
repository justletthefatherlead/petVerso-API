package com.namassacompany.petVersoRestFull.dto;

import com.namassacompany.petVersoRestFull.model.StatusDeVinculo;
import jakarta.validation.constraints.NotNull;

public record ProcessarSolicitacaoDTO(
        @NotNull Long idSolicitacao,
        @NotNull StatusDeVinculo novoStatus
) {
}
