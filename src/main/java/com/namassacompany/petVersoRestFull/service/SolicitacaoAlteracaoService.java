package com.namassacompany.petVersoRestFull.service;

import com.namassacompany.petVersoRestFull.dto.SolicitarAlteracaoResponseDTO;
import com.namassacompany.petVersoRestFull.dto.TarefaRequestDTO;
import com.namassacompany.petVersoRestFull.dto.VacinaRequestDTO;
import com.namassacompany.petVersoRestFull.model.SolicitacaoAlteracao;
import com.namassacompany.petVersoRestFull.model.Usuario;
import com.namassacompany.petVersoRestFull.repository.SolicitacaoAlteracaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Service
public class SolicitarAlteracaoService {
    private final SolicitacaoAlteracaoRepository solicitacaoAlteracaoRepository;
    private final JsonMapper jsonMapper;



    public SolicitarAlteracaoService(SolicitacaoAlteracaoRepository solicitacaoAlteracaoRepository, JsonMapper jsonMapper) {
        this.solicitacaoAlteracaoRepository = solicitacaoAlteracaoRepository;
        this.jsonMapper = jsonMapper;
    }
    @Transactional(readOnly = true)
    public List<SolicitarAlteracaoResponseDTO> listarPendentes(Usuario usuario){
        return solicitacaoAlteracaoRepository.listarPendentesDoDono(usuario).stream().map(this::paraResponse).toList();

    }

    private SolicitarAlteracaoResponseDTO paraResponse(SolicitacaoAlteracao s){
        try{
            return switch (s.getTipo()){
                case ALTERAR_TAREFA -> new SolicitarAlteracaoResponseDTO(s,
                        jsonMapper.readValue(s.getDadosPropostos(), TarefaRequestDTO.class),null);
                case ALTERAR_VACINA, ADICIONAR_VACINA-> new SolicitarAlteracaoResponseDTO(s, null,
                        jsonMapper.readValue(s.getDadosPropostos(), VacinaRequestDTO.class));

            };
        }catch (JacksonException e){
            throw new IllegalStateException("Dados de solicitacao invalidos");

        }
    }
}
