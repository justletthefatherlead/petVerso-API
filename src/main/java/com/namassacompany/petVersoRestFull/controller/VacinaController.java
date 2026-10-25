package com.namassacompany.petVersoRestFull.controller;

import com.namassacompany.petVersoRestFull.dto.VacinaRequestDTO;
import com.namassacompany.petVersoRestFull.dto.VacinaResponseDTO;
import com.namassacompany.petVersoRestFull.model.Usuario;
import com.namassacompany.petVersoRestFull.service.VacinaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.management.DescriptorKey;
import java.util.List;

@RestController
@RequestMapping("/api")
public class VacinaController {
    private final VacinaService vacinaService;

    public VacinaController(VacinaService vacinaService) {
        this.vacinaService = vacinaService;
    }

    @PostMapping("/pets/{idPet}/vacinas")
    public ResponseEntity<VacinaResponseDTO> cadastrar(@PathVariable Long idPet,
                                                       @Valid @RequestBody VacinaRequestDTO dto,
                                                       @AuthenticationPrincipal Usuario usuario) {
        VacinaResponseDTO vacina = vacinaService.cadastrar(idPet, dto, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(vacina);
    }

    @GetMapping("/pets/{idPet}/vacinas")
    public ResponseEntity<List<VacinaResponseDTO>> listar(
            @PathVariable Long idPet,
            @AuthenticationPrincipal Usuario usuario
    ) {
        List<VacinaResponseDTO> vacinas = vacinaService.listar(idPet, usuario);

        return ResponseEntity.ok(vacinas);
    }

    @PutMapping("/vacinas/{idVacina}")
    public ResponseEntity<VacinaResponseDTO> atualizar(@PathVariable Long idVacina,
                                                       @Valid @RequestBody VacinaRequestDTO dto,
                                                       @AuthenticationPrincipal Usuario usuario) {
        VacinaResponseDTO vacinaAtuzaliada = vacinaService.atualizar(idVacina, dto, usuario);
        return ResponseEntity.ok().body(vacinaAtuzaliada);
    }
    @PatchMapping("/vacinas/{idVacina}/aplicar")
    public ResponseEntity<VacinaResponseDTO> marcarComoAplicada(
            @PathVariable Long idVacina,
            @AuthenticationPrincipal Usuario usuario
    ){
        return ResponseEntity.ok(vacinaService.marcarComoAplicada(idVacina,usuario));
    }

    @DeleteMapping("/vacinas/{idVacina}")
    public ResponseEntity<Void> excluirVacina(@PathVariable Long idVacina, @AuthenticationPrincipal Usuario usuario){
        vacinaService.excluir(idVacina, usuario);
        return ResponseEntity.noContent().build();

    }
}
