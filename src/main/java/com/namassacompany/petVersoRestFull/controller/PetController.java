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
    public ResponseEntity<PetResponseDTO> cadastrar(@RequestBody PetRequestDTO petDto, @AuthenticationPrincipal Usuario usuario) {
        PetResponseDTO petCadastrado = petService.cadastrar(petDto, usuario);
        return ResponseEntity.ok(petCadastrado);
    }

    @GetMapping("/{id}/perfil")
    public ResponseEntity<PetPerfilResponseDTO> visualizarPetPerfil(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        PetPerfilResponseDTO visualizarPetPerfil = petService.visualizarPetPerfil(id, usuario);
        return ResponseEntity.ok(visualizarPetPerfil);
    }

    @PutMapping("/{id}/atualizarPetPerfil")
    public ResponseEntity<PetPerfilResponseDTO> addPerfilSensiAndPersonalit(@PathVariable Long id, @RequestBody PetPerfilRequestDTO pdto, @AuthenticationPrincipal Usuario usuario) {
        PetPerfilResponseDTO atualizarPetPerfil = petService.addPerfilSensiAndPersonalit(id, pdto, usuario);
        return ResponseEntity.ok(atualizarPetPerfil);
    }

    @PostMapping("/solicitarVinculo")
    public ResponseEntity<VinculoResponseDTO> solicitarVinculo(@Valid @RequestBody VinculoRequestDTO dto, @AuthenticationPrincipal Usuario usuario) {
        VinculoResponseDTO solicitacao = petService.solicitarVinculo(usuario, dto);
        return ResponseEntity.ok(solicitacao);
    }

    @GetMapping("/listarSolicitacoes")
    public ResponseEntity<List<SolicitacaoResponseDTO>> solicitacoesPendentes(@AuthenticationPrincipal Usuario usuario) {
        List<SolicitacaoResponseDTO> pendentes = petService.listarSolicitacoes(usuario);
        return ResponseEntity.ok(pendentes);


    }

    @PostMapping("/processarSolicitacao")
    public ResponseEntity<VinculoResponseDTO> processarSolicitacao(@Valid @RequestBody ProcessarSolicitacaoRequestDTO dto, @AuthenticationPrincipal Usuario usuario) {
        VinculoResponseDTO resposta = petService.processarSolicitacao(dto.idSolicitacao(), dto.novoStatus(), usuario);

        return ResponseEntity.ok(resposta);

    }

    @GetMapping("/meusPets")
    public ResponseEntity<List<PetResumoResponseDTO>> listarPets(@AuthenticationPrincipal Usuario usuario) {
        List<PetResumoResponseDTO> pets = petService.listarPets(usuario);
        return ResponseEntity.ok(pets);
    }

    @PutMapping("/{id}/foto")
    public ResponseEntity<PetFotoRequestDTO> atualizarFotoPet(@PathVariable Long id, @RequestBody PetFotoRequestDTO dto, @AuthenticationPrincipal Usuario usuario) {
        PetFotoRequestDTO petFotoRequestDTO = petService.atualizarFotoPet(id, usuario, dto);
        return ResponseEntity.ok(petFotoRequestDTO);
    }
}
