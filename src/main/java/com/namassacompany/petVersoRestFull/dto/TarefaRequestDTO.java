package com.namassacompany.petVersoRestFull.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record TarefaRequestDTO(
        @NotBlank String titulo,
        String descricao,
        @NotNull LocalDate dataTarefa,
        LocalTime hora
        ) {
}
