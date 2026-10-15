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
import java.util.List;
import java.util.function.Supplier;

@Service
public class TarefaService {
    private final PetRepository petRepository;
    private final VinculoPetRepository vinculoPetRepository;
    private final TarefaRepository tarefaRepository;

    public TarefaService(PetRepository petRepository, VinculoPetRepository vinculoPetRepository, TarefaRepository tarefaRepository) {
        this.petRepository = petRepository;
        this.vinculoPetRepository = vinculoPetRepository;
        this.tarefaRepository = tarefaRepository;
    }

    @Transactional
    public TarefaResponseDTO criar(Long idPet, TarefaCadastroDTO dto, Usuario usuario) {
        Pet pet = petRepository.findById(idPet).orElseThrow(() -> new PetNaoEncontradoException("Pet nao encontrado"));
        buscarVinculoAceito(pet, usuario, () -> new PetNaoEncontradoException("Pet nao encontrado"));
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
        buscarVinculoAceito(pet, usuario, () -> new TarefaNaoEncontradaException("Pet nao encontrado"));
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
        buscarVinculoAceito(tarefa.getPet(), usuario, () -> new TarefaNaoEncontradaException("Tarefa nao encontrada"));
        if (tarefa.isConcluida()) {
            throw new IllegalStateException("Tarefa já concluida");
        }
        tarefa.setConcluida(true);
        tarefa.setExecutadoPor(usuario);
        tarefa.setDataConclusao(LocalDateTime.now());

        return new TarefaResponseDTO(tarefa);
    }


    private VinculoPet buscarVinculoAceito(Pet pet, Usuario usuario, Supplier<? extends RuntimeException> erro) {
        VinculoPet vinculo = vinculoPetRepository.findByPetAndUsuario(pet, usuario).orElseThrow(erro);
        if (vinculo.getStatus() != StatusDeVinculo.ACEITO) {
            throw erro.get();
        }
        return vinculo;
    }
}
