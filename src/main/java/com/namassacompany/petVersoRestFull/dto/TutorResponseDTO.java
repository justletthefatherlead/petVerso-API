package com.namassacompany.petVersoRestFull.dto;

import com.namassacompany.petVersoRestFull.model.Papel;
import com.namassacompany.petVersoRestFull.model.Usuario;
import com.namassacompany.petVersoRestFull.model.VinculoPet;

import java.time.LocalDateTime;
import java.util.Base64;

public record TutorResponseDTO(
        Long idUsuario,
        String nome,
        Papel papel,
        LocalDateTime dataVinculo,
        String fotoBase64
) {
    private static String nomeExibicao(Usuario usuario) {
        String apelido = usuario.getApelido();
        return (apelido == null || apelido.isBlank()) ? usuario.getNome() : apelido;
    }
    public TutorResponseDTO(VinculoPet vinculo){
        this(vinculo.getUsuario().getIdUsuario(),
                nomeExibicao(vinculo.getUsuario()),
                vinculo.getPapel(),
                vinculo.getDataDeCriacao(),
                (vinculo.getUsuario().getFoto() != null) ? Base64.getEncoder().encodeToString(vinculo.getUsuario().getFoto()): null);
    }
}
