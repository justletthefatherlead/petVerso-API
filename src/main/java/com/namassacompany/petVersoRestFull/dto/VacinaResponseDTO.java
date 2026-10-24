package com.namassacompany.petVersoRestFull.dto;

import com.namassacompany.petVersoRestFull.model.IntervaloUnidade;
import com.namassacompany.petVersoRestFull.model.StatusVacina;
import com.namassacompany.petVersoRestFull.model.Vacina;

import java.time.LocalDate;
import java.time.ZoneId;

public record VacinaResponseDTO(
        Long idVacina,
        String nome,
        LocalDate dataAplicacao,
        boolean dosagemUnica,
        Integer intervaloQuantidade,
        IntervaloUnidade intervaloUnidade,
        LocalDate dataProximaDose,
        String observacao,
        StatusVacina statusVacina
) {
    public VacinaResponseDTO(Vacina vacina) {
        this(vacina.getIdVacina(),
                vacina.getNome(),
                vacina.getDataAplicacao(),
                vacina.isDosagemUnica(),
                vacina.getIntervaloQuantidade(),
                vacina.getIntervaloUnidade(),
                vacina.getDataProximaDose(),
                vacina.getObservacao(),
                calcularStatus(vacina));
    }

    private static StatusVacina calcularStatus(Vacina vacina) {
        if (vacina.getStatus() == StatusVacina.PENDENTE
                && vacina.getDataAplicacao() != null
                && vacina.getDataAplicacao().isBefore(LocalDate.now(ZoneId.of("America/Sao_Paulo")))) {
            return StatusVacina.ATRASADA;

        }
        return vacina.getStatus();
    }
}
