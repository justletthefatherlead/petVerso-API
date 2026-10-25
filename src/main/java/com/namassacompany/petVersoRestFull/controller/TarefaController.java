package com.namassacompany.petVersoRestFull.controller;

import com.namassacompany.petVersoRestFull.dto.TarefaRequestDTO;
import com.namassacompany.petVersoRestFull.dto.TarefaResponseDTO;
import com.namassacompany.petVersoRestFull.model.Usuario;
import com.namassacompany.petVersoRestFull.service.TarefaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(("/api"))
public class TarefaController {
    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }


    @PostMapping("/pets/{id}/tarefas")
    public ResponseEntity<TarefaResponseDTO> criar(
            @Valid @RequestBody TarefaRequestDTO dto,
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario
    ) {
        TarefaResponseDTO tarefaResponseDTO = tarefaService.criar(id, dto, usuario);
        return ResponseEntity.ok(tarefaResponseDTO);
    }


    @GetMapping("/pets/{id}/tarefas")
    public ResponseEntity<List<TarefaResponseDTO>> listarTarefas(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario
    ) {
        List<TarefaResponseDTO> tarefas = tarefaService.listarTarefas(id, usuario);
        return ResponseEntity.ok(tarefas);
    }

    @GetMapping("/tarefas")
    public ResponseEntity<List<TarefaResponseDTO>> listarPorDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @AuthenticationPrincipal Usuario usuario
    ) {
        List<TarefaResponseDTO> tarefasDoDia = tarefaService.listarPorDia(data, usuario);
        return ResponseEntity.ok(tarefasDoDia);

    }

    @PatchMapping("/tarefas/{idTarefa}/concluir")
    public ResponseEntity<TarefaResponseDTO> concluirTarefa(
            @PathVariable Long idTarefa,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.ok(tarefaService.concluirTarefa(idTarefa, usuario));
    }

    @PutMapping("/tarefas/{idTarefa}")
    public ResponseEntity<TarefaResponseDTO> atualizarTarefa(
            @PathVariable Long idTarefa,
            @Valid @RequestBody TarefaRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario
    ){
        return ResponseEntity.ok(tarefaService.atualizarTarefa(idTarefa,dto, usuario));

    }

    @DeleteMapping("/tarefas/{idTarefa}")
    public ResponseEntity<Void> excluirTarefa(@PathVariable Long idTarefa,
                                              @AuthenticationPrincipal Usuario usuario){
        tarefaService.excluir(idTarefa, usuario);
        return ResponseEntity.noContent().build();
    }

}
