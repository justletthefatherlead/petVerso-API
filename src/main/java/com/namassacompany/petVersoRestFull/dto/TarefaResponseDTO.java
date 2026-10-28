package com.namassacompany.petVersoRestFull.dto;

import com.namassacompany.petVersoRestFull.model.Tarefa;
import com.namassacompany.petVersoRestFull.model.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record TarefaResponseDTO(
        Long idTarefa,
        String titulo,
        String descricao,
        LocalDate dataTarefa,
        LocalTime hora,
        String criadaPor,
        boolean concluida,
        Long idPet,
        String nomePet,
        String executadoPorNome,
        LocalDateTime dataConclusao
) {
    private static String nomeDoExecutor(Tarefa tarefa){
        Usuario executor = tarefa.getExecutadoPor();
        if (executor == null){
            return null;
        }
        if (executor.getApelido() == null || executor.getApelido().isBlank()){
            return executor.getNome();
        }
        return executor.getApelido();

    }
    public TarefaResponseDTO(Tarefa tarefa){
        this(tarefa.getIdTarefa(), tarefa.getTitulo(),tarefa.getDescricao(),tarefa.getDataTarefa(),
                tarefa.getHora(),tarefa.getCriadaPor().getNome(), tarefa.isConcluida(),tarefa.getPet().getIdPet(),tarefa.getPet().getNome(), nomeDoExecutor(tarefa), tarefa.getDataConclusao());
    }
}
