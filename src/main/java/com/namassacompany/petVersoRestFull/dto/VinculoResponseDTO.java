package com.namassacompany.petVersoRestFull.dto;

import com.namassacompany.petVersoRestFull.model.StatusDeVinculo;

import java.time.LocalDateTime;

public record VinculoResponseDTO(
        boolean sucesso,
        String mensagem,
        StatusDeVinculo statusDeVinculo,
        DadosVinculoResponseDTO dadosVinculoResponseDTO
) {
    public record DadosVinculoResponseDTO(
            Long idSolicitacao,
            String codigoUtilizado,
            String nomePet,
            LocalDateTime dataSolicitacao

    ){}
}
