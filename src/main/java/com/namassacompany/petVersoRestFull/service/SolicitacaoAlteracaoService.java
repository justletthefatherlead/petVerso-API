package com.namassacompany.petVersoRestFull.service;

import com.namassacompany.petVersoRestFull.dto.RecusarSolicitacaoResponseDTO;
import com.namassacompany.petVersoRestFull.dto.SolicitarAlteracaoResponseDTO;
import com.namassacompany.petVersoRestFull.dto.TarefaRequestDTO;
import com.namassacompany.petVersoRestFull.dto.VacinaRequestDTO;
import com.namassacompany.petVersoRestFull.exception.*;
import com.namassacompany.petVersoRestFull.model.*;
import com.namassacompany.petVersoRestFull.repository.PetRepository;
import com.namassacompany.petVersoRestFull.repository.SolicitacaoAlteracaoRepository;
import com.namassacompany.petVersoRestFull.repository.TarefaRepository;
import com.namassacompany.petVersoRestFull.repository.VacinaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.namassacompany.petVersoRestFull.model.TipoAlteracao.*;
@Service
public class SolicitacaoAlteracaoService {
    private final SolicitacaoAlteracaoRepository solicitacaoAlteracaoRepository;
    private final JsonMapper jsonMapper;
    private final TarefaRepository tarefaRepository;
    private final VinculoAcessoService vinculoAcessoService;
    private final PetRepository petRepository;
    private final VacinaService vacinaService;
    private final VacinaRepository vacinaRepository;
    private final TarefaService tarefaService;


    public SolicitacaoAlteracaoService(SolicitacaoAlteracaoRepository solicitacaoAlteracaoRepository, JsonMapper jsonMapper, TarefaRepository tarefaRepository, VinculoAcessoService vinculoAcessoService, PetRepository petRepository, VacinaService vacinaService, VacinaRepository vacinaRepository, TarefaService tarefaService) {
        this.solicitacaoAlteracaoRepository = solicitacaoAlteracaoRepository;
        this.jsonMapper = jsonMapper;
        this.tarefaRepository = tarefaRepository;
        this.vinculoAcessoService = vinculoAcessoService;
        this.petRepository = petRepository;
        this.vacinaService = vacinaService;
        this.vacinaRepository = vacinaRepository;
        this.tarefaService = tarefaService;
    }
    @Transactional(readOnly = true)
    public List<SolicitarAlteracaoResponseDTO> listarPendentes(Usuario usuario){
        return solicitacaoAlteracaoRepository.listarPendentesDoDono(usuario).stream().map(this::paraResponse).toList();

    }
    @Transactional
    public SolicitarAlteracaoResponseDTO solicitarAlteracaoTarefa(Long idTarefa, TarefaRequestDTO dados, Usuario usuario){
        Tarefa tarefa = tarefaRepository.findById(idTarefa).orElseThrow(()->new TarefaNaoEncontradaException("Tarefa nao encontrada"));
        VinculoPet vinculo = vinculoAcessoService.buscarVinculoAceito(tarefa.getPet(), usuario,()->new TarefaNaoEncontradaException("Tarefa nao encontrada"));
        if (vinculo.getPapel() == Papel.DONO)
            throw new AcessoNegadoException("O dono edita as tarefas diretamente");
        if (tarefa.isConcluida())
            throw new IllegalStateException("Tarefa ja concluida nao pode ser alterada");
        if (solicitacaoAlteracaoRepository.existsBySolicitanteAndTipoAndIdAlvoAndStatus(usuario, ALTERAR_TAREFA,idTarefa, StatusSolicitacao.PENDENTE))
            throw new IllegalStateException("Ja existe uma alteracao aguardando aprovacao");
        return registrar(vinculo.getPet(),usuario, ALTERAR_TAREFA,idTarefa,dados);
    }

    @Transactional
    public SolicitarAlteracaoResponseDTO solicitarAdicaoVacina(Long idPet, VacinaRequestDTO dados, Usuario usuario){
        Pet pet = petRepository.findById(idPet).orElseThrow(()-> new PetNaoEncontradoException("Pet nao encontrado"));
        VinculoPet vinculo = vinculoAcessoService.buscarVinculoAceito(pet, usuario,()-> new PetNaoEncontradoException("Pet nao encontrado"));
        if (vinculo.getPapel() == Papel.DONO) throw new AcessoNegadoException("O dono edita vacinas diretamente");
        dados.validarRegras();
        return registrar(pet,usuario, ADICIONAR_VACINA,null,dados);

    }
    @Transactional
    public SolicitarAlteracaoResponseDTO solicitarAlteracaoVacina(Long idVacia, VacinaRequestDTO dados, Usuario usuario){
        Vacina vacina = vacinaRepository.findById(idVacia).orElseThrow(()-> new VacinaNaoEncontradaException("Vacina nao encontrada"));
        VinculoPet vinculo = vinculoAcessoService.buscarVinculoAceito(vacina.getPet(),usuario, ()-> new VacinaNaoEncontradaException("Vacina nao encontrada"));
        if (vinculo.getPapel() == Papel.DONO){
            throw new AcessoNegadoException("O dono edita as vacinas diretamente");
        }
        vacinaService.validarAtualizacao(vacina, dados);

        if (solicitacaoAlteracaoRepository.existsBySolicitanteAndTipoAndIdAlvoAndStatus(usuario, ALTERAR_VACINA,idVacia,StatusSolicitacao.PENDENTE))
        throw new IllegalStateException("Ja existe uma alteracao aguardando aprovacao");

       return  registrar(vacina.getPet(), usuario, ALTERAR_VACINA, idVacia,dados);
    }

    @Transactional
    public SolicitarAlteracaoResponseDTO recusarSolicitacao(Long idSolicitacao, String motivo,Usuario dono){
        SolicitacaoAlteracao solicitacao = solicitacaoAlteracaoRepository.buscarParaResponder(idSolicitacao).orElseThrow(()-> new SolicitacaoNaoEncontradaException("Solicitacao nao encontrada"));
         vinculoAcessoService.buscarVinculoDono(solicitacao.getPet(), dono, ()-> new SolicitacaoNaoEncontradaException("Solicitacao nao encontrada"));
        if(solicitacao.getStatus() != StatusSolicitacao.PENDENTE) throw new IllegalStateException("Esta solicitacao ja foi respondida");
        solicitacao.setStatus(StatusSolicitacao.RECUSADA);
        solicitacao.setDataResposta(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
        solicitacao.setRespondidoPor(dono);
        solicitacao.setMotivoRecusa(motivo == null || motivo.isBlank() ? null : motivo.trim());

        return paraResponse(solicitacao);
    }
    @Transactional
    public SolicitarAlteracaoResponseDTO aprovarSolicitacao(Long idSolicitacao, Usuario dono){
        SolicitacaoAlteracao solicitacao = solicitacaoAlteracaoRepository.buscarParaResponder(idSolicitacao).orElseThrow(()-> new SolicitacaoNaoEncontradaException("Solicitacao nao encontrada"));
        vinculoAcessoService.buscarVinculoDono(solicitacao.getPet(), dono,()-> new SolicitacaoNaoEncontradaException("Solicitacao nao encontrada"));
        if(solicitacao.getStatus() != StatusSolicitacao.PENDENTE) throw new IllegalStateException("Esta solicitacao ja foi respondida");
       try {
           switch (solicitacao.getTipo()) {
               case ALTERAR_TAREFA -> tarefaService.atualizarTarefa(solicitacao.getIdAlvo(),
                       jsonMapper.readValue(solicitacao.getDadosPropostos(), TarefaRequestDTO.class), dono);
               case ALTERAR_VACINA -> vacinaService.atualizar(solicitacao.getIdAlvo(),
                       jsonMapper.readValue(solicitacao.getDadosPropostos(), VacinaRequestDTO.class), dono);
               case ADICIONAR_VACINA -> vacinaService.cadastrar(solicitacao.getPet().getIdPet(),
                       jsonMapper.readValue(solicitacao.getDadosPropostos(), VacinaRequestDTO.class), dono);
               default-> throw new IllegalStateException("Dados de solicitacao invalidos");
           }

       } catch (JacksonException ex) {
           throw new IllegalStateException("Dados de solicitacao invalidos");
       }
       solicitacao.setStatus(StatusSolicitacao.APROVADA);
       solicitacao.setDataResposta(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
       solicitacao.setRespondidoPor(dono);
       return  paraResponse(solicitacao);

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

    private  SolicitarAlteracaoResponseDTO registrar(Pet pet, Usuario solicitante, TipoAlteracao tipoAlteracao, Long idAlvo, Object dados){
        String json =  jsonMapper.writeValueAsString(dados);
        SolicitacaoAlteracao s = new SolicitacaoAlteracao(pet, solicitante, tipoAlteracao, idAlvo, json);
        solicitacaoAlteracaoRepository.save(s);
        return paraResponse(s);
    }
}
