package com.namassacompany.petVersoRestFull.service;

import com.namassacompany.petVersoRestFull.dto.SolicitarAlteracaoResponseDTO;
import com.namassacompany.petVersoRestFull.dto.TarefaRequestDTO;
import com.namassacompany.petVersoRestFull.dto.VacinaRequestDTO;
import com.namassacompany.petVersoRestFull.exception.AcessoNegadoException;
import com.namassacompany.petVersoRestFull.exception.PetNaoEncontradoException;
import com.namassacompany.petVersoRestFull.exception.TarefaNaoEncontradaException;
import com.namassacompany.petVersoRestFull.exception.VacinaNaoEncontradaException;
import com.namassacompany.petVersoRestFull.model.*;
import com.namassacompany.petVersoRestFull.repository.PetRepository;
import com.namassacompany.petVersoRestFull.repository.SolicitacaoAlteracaoRepository;
import com.namassacompany.petVersoRestFull.repository.TarefaRepository;
import com.namassacompany.petVersoRestFull.repository.VacinaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Service
public class SolicitacaoAlteracaoService {
    private final SolicitacaoAlteracaoRepository solicitacaoAlteracaoRepository;
    private final JsonMapper jsonMapper;
    private final TarefaRepository tarefaRepository;
    private final VinculoAcessoService vinculoAcessoService;
    private final PetRepository petRepository;
    private final VacinaService vacinaService;
    private final VacinaRepository vacinaRepository;



    public SolicitacaoAlteracaoService(SolicitacaoAlteracaoRepository solicitacaoAlteracaoRepository, JsonMapper jsonMapper, TarefaRepository tarefaRepository, VinculoAcessoService vinculoAcessoService, PetRepository petRepository, VacinaService vacinaService, VacinaRepository vacinaRepository) {
        this.solicitacaoAlteracaoRepository = solicitacaoAlteracaoRepository;
        this.jsonMapper = jsonMapper;
        this.tarefaRepository = tarefaRepository;
        this.vinculoAcessoService = vinculoAcessoService;
        this.petRepository = petRepository;
        this.vacinaService = vacinaService;
        this.vacinaRepository = vacinaRepository;
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
            throw new AcessoNegadoException("Tarefa ja concluida nao pode ser alterada");
        if (solicitacaoAlteracaoRepository.existsBySolicitanteAndTipoAndIdAlvoAndStatus(usuario, TipoAlteracao.ALTERAR_TAREFA,idTarefa, StatusSolicitacao.PENDENTE))
            throw new IllegalStateException("Ja existe uma alteracao aguardando aprovacao");
        return registrar(vinculo.getPet(),usuario,TipoAlteracao.ALTERAR_TAREFA,idTarefa,dados);
    }

    @Transactional
    public SolicitarAlteracaoResponseDTO solicitarAdicaoVacina(Long idPet, VacinaRequestDTO dados, Usuario usuario){
        Pet pet = petRepository.findById(idPet).orElseThrow(()-> new PetNaoEncontradoException("Pet nao encontrado"));
        VinculoPet vinculo = vinculoAcessoService.buscarVinculoAceito(pet, usuario,()-> new PetNaoEncontradoException("Pet nao encontrado"));
        if (vinculo.getPapel() == Papel.DONO) throw new AcessoNegadoException("O dono edita vacinas diretamente");
        dados.validarRegras();
        return registrar(pet,usuario,TipoAlteracao.ADICIONAR_VACINA,null,dados);

    }
    @Transactional
    public SolicitarAlteracaoResponseDTO solicitarAlteracaoVacina(Long idVacia, VacinaRequestDTO dados, Usuario usuario){
        Vacina vacina = vacinaRepository.findById(idVacia).orElseThrow(()-> new VacinaNaoEncontradaException("Vacina nao encontrada"));
        VinculoPet vinculo = vinculoAcessoService.buscarVinculoAceito(vacina.getPet(),usuario, ()-> new VacinaNaoEncontradaException("Vacina nao encontrada"));
        if (vinculo.getPapel() == Papel.DONO){
            throw new AcessoNegadoException("O dono edita as vacinas diretamente");
        }
        vacinaService.validarAtualizacao(vacina, dados);

        if (solicitacaoAlteracaoRepository.existsBySolicitanteAndTipoAndIdAlvoAndStatus(usuario,TipoAlteracao.ALTERAR_VACINA,idVacia,StatusSolicitacao.PENDENTE))
        throw new IllegalStateException("Ja existe uma alteracao aguardando aprovacao");

       return  registrar(vacina.getPet(), usuario,TipoAlteracao.ALTERAR_VACINA, idVacia,dados);
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
