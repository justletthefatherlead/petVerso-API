package com.namassacompany.petVersoRestFull.controller;

import com.namassacompany.petVersoRestFull.dto.*;
import com.namassacompany.petVersoRestFull.model.Usuario;
import com.namassacompany.petVersoRestFull.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;

    }

    @PostMapping("/cadastrar")
    public ResponseEntity<UsuarioCadastroResponseDTO> cadastrar(@Valid @RequestBody UsuarioRequestDTO usuario) {
        UsuarioCadastroResponseDTO novoUsuario = usuarioService.cadastrar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(novoUsuario);
    }

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioPerfilResponseDTO> perfilUsuario(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioService.vizualizarPerfil(usuario));

    }

    @PutMapping("/atualizarPerfil")
    public ResponseEntity<UsuarioPerfilResponseDTO> atualizarPerfil(
            @RequestBody UsuarioPerfilRequestDTO atUsario,
            @AuthenticationPrincipal Usuario usuario
    ) {
        UsuarioPerfilResponseDTO perfilAtualizado = usuarioService.atualizarPerfil(atUsario, usuario);
        return ResponseEntity.ok(perfilAtualizado);


    }
}


