package com.namassacompany.petVersoRestFull.dto;

import com.namassacompany.petVersoRestFull.model.VinculoPet;

import java.util.Base64;

public record PetResumoResponseDTO(
        Long idPet,
        String nome,
        String papel,
        String fotoPetBase64
) {
    public PetResumoResponseDTO(VinculoPet vinculo){
        this(vinculo.getPet().getIdPet(),
             vinculo.getPet().getNome(),
             vinculo.getPapel().name(),
                (vinculo.getPet().getFoto() != null) ? Base64.getEncoder().encodeToString(vinculo.getPet().getFoto()): null);
    }
}
