package com.namassacompany.petVersoRestFull.dto;


import com.namassacompany.petVersoRestFull.exception.DadosInvalidosException;
import com.namassacompany.petVersoRestFull.model.IntervaloUnidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.ZoneId;

public record VacinaRequestDTO(
        @NotBlank String nome,
        LocalDate dataAplicacao,
        @NotNull Boolean dosagemUnica,
        @Positive Integer intervaloQuantidade,
        IntervaloUnidade intervaloUnidade,
        @NotNull Boolean jaAplicada,
        @Size(max = 500) String observacao
        ) {

        private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
        public void validarRegras(){
                validarIntervalo();
                validarDataSeAplicada();
        }

        private void validarIntervalo() {
                if (dosagemUnica()) {
                        if (intervaloUnidade() != null || intervaloQuantidade() != null) {
                                throw new DadosInvalidosException("Dose unica nao pode ter intervalos");
                        }
                        return;
                }
                if (intervaloUnidade() == null || intervaloQuantidade() == null) {
                        throw new DadosInvalidosException("Informe a quantidade e a unidade do intervalo");
                }
                if (intervaloQuantidade() <= 0) {
                        throw new DadosInvalidosException("A quantidade do intervalo deve ser maior que zero");

                }
        }
        private void validarDataSeAplicada() {
                if (!jaAplicada()) {
                        return;
                }
                if (dataAplicacao() == null) {
                        throw new DadosInvalidosException("Informe a data de aplicacao da vacina");
                }
                if (dataAplicacao().isAfter(LocalDate.now(ZONA))) {
                        throw new DadosInvalidosException("Vacina ja aplicada nao pode ter data futura");
                }
        }

}
