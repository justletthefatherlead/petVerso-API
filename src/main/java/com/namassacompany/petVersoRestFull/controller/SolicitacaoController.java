package com.namassacompany.petVersoRestFull.controller;

import com.namassacompany.petVersoRestFull.dto.PedidosPendentesResponseDTO;
import com.namassacompany.petVersoRestFull.dto.SolicitarAlteracaoResponseDTO;
import com.namassacompany.petVersoRestFull.dto.TarefaRequestDTO;
import com.namassacompany.petVersoRestFull.dto.VacinaRequestDTO;
import com.namassacompany.petVersoRestFull.model.Usuario;
import com.namassacompany.petVersoRestFull.service.PedidosPendentesService;
import com.namassacompany.petVersoRestFull.service.SolicitacaoAlteracaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {

    private final PedidosPendentesService pedidosPendentesService;
    private final SolicitacaoAlteracaoService solicitacaoAlteracaoService;

    public SolicitacaoController(PedidosPendentesService pedidosPendentesService, SolicitacaoAlteracaoService solicitacaoAlteracaoService) {
        this.pedidosPendentesService = pedidosPendentesService;
        this.solicitacaoAlteracaoService = solicitacaoAlteracaoService;
    }

    @GetMapping("/pendentes")
    public ResponseEntity<PedidosPendentesResponseDTO> listarPendentes(
            @AuthenticationPrincipal Usuario usuario
    ){
        return ResponseEntity.ok(pedidosPendentesService.listar(usuario));
    }
    @PostMapping("/tarefas/{idTarefa}")
    public ResponseEntity<SolicitarAlteracaoResponseDTO> solicitarAlteracaoTarefa(@PathVariable  Long idTarefa,
                                                                                  @Valid @RequestBody TarefaRequestDTO dados,
                                                                                  @AuthenticationPrincipal Usuario usuario){
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitacaoAlteracaoService.solicitarAlteracaoTarefa(idTarefa,dados, usuario));
    }

    @PostMapping("/pets/{idPet}/vacinas")
    public ResponseEntity<SolicitarAlteracaoResponseDTO> solicitarAdicaoVacina(@PathVariable Long idPet,
                                                                               @Valid @RequestBody VacinaRequestDTO dados,
                                                                               @AuthenticationPrincipal Usuario usuario){
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitacaoAlteracaoService.solicitarAdicaoVacina(idPet,dados,usuario));
    }

    @PostMapping("/vacinas/{idVacina}")
    public ResponseEntity<SolicitarAlteracaoResponseDTO> solicitarAlteracaoVacina(@PathVariable Long idVacina,
                                                                                  @Valid @RequestBody VacinaRequestDTO dados,
                                                                                  @AuthenticationPrincipal Usuario usuario){
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitacaoAlteracaoService.solicitarAlteracaoVacina(idVacina,dados,usuario));
    }

}
