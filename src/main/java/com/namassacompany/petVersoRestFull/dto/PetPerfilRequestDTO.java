package com.namassacompany.petVersoRestFull.dto;

import java.util.List;

public record PetPerfilRequestDTO(
        String perfilDeSensibilidade,
        List<String> personalidades
) {
}
