package com.namassacompany.petVersoRestFull.dto;

public record UsuarioCadastroResponseDTO(
        Long idUsuario,
        String nomeDeUsuario,
        String email,
        String token
) {}
