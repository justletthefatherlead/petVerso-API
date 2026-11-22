package com.namassacompany.petVersoRestFull.dto;

import java.util.List;

public record PedidosPendentesResponseDTO (
        List<SolicitacaoResponseDTO> vinculos,
        List<SolicitarAlteracaoResponseDTO> alteracoes
){}
