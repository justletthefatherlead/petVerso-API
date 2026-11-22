package com.namassacompany.petVersoRestFull.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.namassacompany.petVersoRestFull.model.SolicitacaoAlteracao;
import com.namassacompany.petVersoRestFull.model.StatusSolicitacao;
import com.namassacompany.petVersoRestFull.model.TipoAlteracao;
import com.namassacompany.petVersoRestFull.model.Usuario;

import java.time.LocalDateTime;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SolicitarAlteracaoResponseDTO(
        Long idSolicitacao,
        TipoAlteracao tipo,
        StatusSolicitacao status,
        Long idAlvo,
        Long idPet,
        String nomePet,
        String nomeSolicitante,
        LocalDateTime dataDeCriacao,
        TarefaRequestDTO dadosTarefa,
        VacinaRequestDTO dadosVacina
) {
    public SolicitarAlteracaoResponseDTO( SolicitacaoAlteracao s,
                                          TarefaRequestDTO dadosTarefa,
                                          VacinaRequestDTO dadosVacina
                                          ){
        this(s.getIdSolicitacao(),s.getTipo(),s.getStatus(),s.getIdAlvo(),s.getPet().getIdPet(),s.getPet().getNome(),nomeExibicao(s.getSolicitante()), s.getDataCriacao(),dadosTarefa,dadosVacina);

    }
    private static  String nomeExibicao(Usuario u){
        String apelido = u.getApelido();
        return (apelido == null || apelido.isBlank()) ? u.getNome() : apelido;
    }
}
