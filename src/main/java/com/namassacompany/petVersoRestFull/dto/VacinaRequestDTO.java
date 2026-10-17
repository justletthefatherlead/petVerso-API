package com.namassacompany.petVersoRestFull.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record VacinaRequestDTO(
        @NotBlank String nome,
        @NotNull LocalDate dataAplicacao,
        LocalDate dataProximaDose,
        @Size(max = 500) String observacao
        ) {

}
