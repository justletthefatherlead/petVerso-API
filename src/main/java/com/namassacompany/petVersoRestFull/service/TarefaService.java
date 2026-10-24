package com.namassacompany.petVersoRestFull.service;

import com.namassacompany.petVersoRestFull.dto.TarefaCadastroDTO;
import com.namassacompany.petVersoRestFull.dto.TarefaResponseDTO;
import com.namassacompany.petVersoRestFull.exception.AcessoNegadoException;
import com.namassacompany.petVersoRestFull.exception.PetNaoEncontradoException;
import com.namassacompany.petVersoRestFull.exception.TarefaNaoEncontradaException;
import com.namassacompany.petVersoRestFull.model.*;
import com.namassacompany.petVersoRestFull.repository.PetRepository;
import com.namassacompany.petVersoRestFull.repository.TarefaRepository;
import com.namassacompany.petVersoRestFull.repository.VinculoPetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Supplier;

@Service
public class TarefaService {
    private final PetRepository petRepository;
    private final VinculoAcessoService vinculoAcessoService;
    private final TarefaRepository tarefaRepository;

    public TarefaService(PetRepository petRepository, VinculoAcessoService vinculoAcessoService, TarefaRepository tarefaRepository) {
        this.petRepository = petRepository;
        this.vinculoAcessoService = vinculoAcessoService;
        this.tarefaRepository = tarefaRepository;
    }

    @Transactional
    public TarefaResponseDTO criar(Long idPet, TarefaCadastroDTO dto, Usuario usuario) {
        Pet pet = petRepository.findById(idPet).orElseThrow(() -> new PetNaoEncontradoException("Pet nao encontrado"));
        vinculoAcessoService.buscarVinculoAceito(pet, usuario, () -> new PetNaoEncontradoException("Pet nao encontrado"));
        Tarefa tarefa = new Tarefa(
                pet,
                dto.titulo(),
                dto.descricao(),
                dto.dataTarefa(),
                dto.hora()
        );
        return new TarefaResponseDTO(tarefaRepository.save(tarefa));


    }

    @Transactional(readOnly = true)
    public List<TarefaResponseDTO> listarTarefas(Long idPet, Usuario usuario) {
        Pet pet = petRepository.findById(idPet).orElseThrow(() -> new PetNaoEncontradoException("Pet nao encontrado"));
        vinculoAcessoService.buscarVinculoAceito(pet, usuario, () -> new TarefaNaoEncontradaException("Pet nao encontrado"));
        List<Tarefa> tarefas = tarefaRepository.findByPetIdPetOrderByDataTarefaAscHoraAsc(idPet);
        return tarefas.stream().map(TarefaResponseDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public List<TarefaResponseDTO> listarPorDia(LocalDate data, Usuario usuario) {
        List<Tarefa> tarefas = tarefaRepository.listarDoDia(usuario, StatusDeVinculo.ACEITO, data);
        return tarefas.stream().map(TarefaResponseDTO::new).toList();
    }

    @Transactional
    public TarefaResponseDTO concluirTarefa(Long idTarefa, Usuario usuario) {
        Tarefa tarefa = tarefaRepository.findById(idTarefa).orElseThrow(() -> new TarefaNaoEncontradaException("Tarefa nao encontrada"));
        vinculoAcessoService.buscarVinculoAceito(tarefa.getPet(), usuario, () -> new TarefaNaoEncontradaException("Tarefa nao encontrada"));
        if (tarefa.isConcluida()) {
            throw new IllegalStateException("Tarefa já concluida");
        }
        tarefa.setConcluida(true);
        tarefa.setExecutadoPor(usuario);
        tarefa.setDataConclusao(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));

        return new TarefaResponseDTO(tarefa);
    }

    @Transactional
    public TarefaResponseDTO atualizarTarefa(Long idTarefa, TarefaCadastroDTO dto, Usuario usuario) {
        Tarefa tarefa = tarefaRepository.findById(idTarefa).orElseThrow(() -> new TarefaNaoEncontradaException("Tarefa nao encontrada"));
        vinculoAcessoService.buscarVinculoDono(tarefa.getPet(), usuario, () -> new TarefaNaoEncontradaException("Tarefa nao encontrada"));

        if (tarefa.isConcluida()) throw new IllegalStateException("Esta tarefa já foi concluida");

        tarefa.setTitulo(dto.titulo());
        tarefa.setDescricao(dto.descricao());
        tarefa.setDataTarefa(dto.dataTarefa());
        tarefa.setHora(dto.hora());

        return new TarefaResponseDTO(tarefa);
    }



}
