package com.namassacompany.petVersoRestFull.dto;

import com.namassacompany.petVersoRestFull.model.Porte;
import com.namassacompany.petVersoRestFull.model.Sexo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PetRequestDTO(
        String fotoBase64,
        @NotBlank(message = "o campo nome é obrigatório") String nome,
        @NotBlank(message = "o campo raca é obrigatório") String raca,
        @NotBlank(message = "o campo especie é obrigatório") String especie,
        @NotNull(message = "pode ser a data de adoção, caso nao saiba a data") LocalDate dataDeNascimento,
        @NotNull Porte porte,
        @NotNull(message = "se nao souber informe um peso aproximado") Double peso,
        @NotNull Sexo sexo
) {}
