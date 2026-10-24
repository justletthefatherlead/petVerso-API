package com.namassacompany.petVersoRestFull.dto;


import com.namassacompany.petVersoRestFull.model.IntervaloUnidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record VacinaRequestDTO(
        @NotBlank String nome,
        LocalDate dataAplicacao,
        @NotNull Boolean dosagemUnica,
        @Positive Integer intervaloQuantidade,
        IntervaloUnidade intervaloUnidade,
        @NotNull Boolean jaAplicada,
        @Size(max = 500) String observacao
        ) {

}
