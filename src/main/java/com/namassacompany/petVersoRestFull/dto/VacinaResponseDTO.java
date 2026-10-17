package com.namassacompany.petVersoRestFull.dto;

import com.namassacompany.petVersoRestFull.model.Vacina;

import java.time.LocalDate;

public record VacinaResponseDTO(
        Long idVacina,
        String nome,
        LocalDate dataAplicacao,
        LocalDate dataProximaDose,
        String observacao
) {
    public VacinaResponseDTO(Vacina vacina){
            this(vacina.getIdVacina(),
                 vacina.getNome(),
                 vacina.getDataAplicacao(),
                 vacina.getDataProximaDose(),
                 vacina.getObservacao());
    }
}
