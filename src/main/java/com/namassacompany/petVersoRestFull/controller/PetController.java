package com.namassacompany.petVersoRestFull.controller;

import com.namassacompany.petVersoRestFull.dto.*;
import com.namassacompany.petVersoRestFull.model.Usuario;
import com.namassacompany.petVersoRestFull.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {
    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }


    @PostMapping("/cadastrar")
    public ResponseEntity<PetCadastroResponseDTO> cadastrar(@RequestBody PetCadastroDTO petDto, @AuthenticationPrincipal Usuario usuario) {
        PetCadastroResponseDTO petCadastrado = petService.cadastrar(petDto, usuario);
        return ResponseEntity.ok(petCadastrado);
    }

    @GetMapping("/{id}/perfil")
    public ResponseEntity<PetPerfilDTO> visualizarPetPerfil(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        PetPerfilDTO visualizarPetPerfil = petService.visualizarPetPerfil(id, usuario);
        return ResponseEntity.ok(visualizarPetPerfil);
    }

    @PutMapping("/{id}/atualizarPetPerfil")
    public ResponseEntity<PetPerfilDTO> addPerfilSensiAndPersonalit(@PathVariable Long id, @RequestBody AtualizarPetDTO pdto, @AuthenticationPrincipal Usuario usuario) {
        PetPerfilDTO atualizarPetPerfil = petService.addPerfilSensiAndPersonalit(id, pdto, usuario);
        return ResponseEntity.ok(atualizarPetPerfil);
    }

    @PostMapping("/solicitarVinculo")
    public ResponseEntity<SolicitarVinculoResponseDTO> solicitarVinculo(@Valid @RequestBody SolicitarVinculoDTO dto, @AuthenticationPrincipal Usuario usuario) {
        SolicitarVinculoResponseDTO solicitacao = petService.solicitarVinculo(usuario, dto);
        return ResponseEntity.ok(solicitacao);
    }

    @GetMapping("/listarSolicitacoes")
    public ResponseEntity<List<SolicitacaoPendenteDTO>> solicitacoesPendentes(@AuthenticationPrincipal Usuario usuario) {
        List<SolicitacaoPendenteDTO> pendentes = petService.listarSolicitacoes(usuario);
        return ResponseEntity.ok(pendentes);


    }

    @PostMapping("/processarSolicitacao")
    public ResponseEntity<SolicitarVinculoResponseDTO> processarSolicitacao(@Valid @RequestBody ProcessarSolicitacaoDTO dto, @AuthenticationPrincipal Usuario usuario) {
        SolicitarVinculoResponseDTO resposta = petService.processarSolicitacao(dto.idSolicitacao(), dto.novoStatus(), usuario);

        return ResponseEntity.ok(resposta);

    }

    @GetMapping("/meusPets")
    public ResponseEntity<List<PetResumoDTO>> listarPets(@AuthenticationPrincipal Usuario usuario) {
        List<PetResumoDTO> pets = petService.listarPets(usuario);
        return ResponseEntity.ok(pets);
    }

    @PutMapping("/{id}/foto")
    public ResponseEntity<AtualizarFotoPetDTO> atualizarFotoPet(@PathVariable Long id, @RequestBody AtualizarFotoPetDTO dto, @AuthenticationPrincipal Usuario usuario) {
        AtualizarFotoPetDTO atualizarFotoPetDTO = petService.atualizarFotoPet(id, usuario, dto);
        return ResponseEntity.ok(atualizarFotoPetDTO);
    }
}
