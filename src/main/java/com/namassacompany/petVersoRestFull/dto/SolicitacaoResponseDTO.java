package com.namassacompany.petVersoRestFull.dto;

import java.time.LocalDateTime;

public record SolicitacaoResponseDTO(
        Long idSolicitacao,
        String nomeSolicitante,
        String nomePet,
        LocalDateTime dataDeCriacao

) {
}
