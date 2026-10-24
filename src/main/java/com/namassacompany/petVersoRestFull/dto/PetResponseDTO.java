package com.namassacompany.petVersoRestFull.dto;

import com.namassacompany.petVersoRestFull.model.Pet;
import com.namassacompany.petVersoRestFull.model.Porte;
import com.namassacompany.petVersoRestFull.model.Sexo;

import java.time.LocalDate;

public record PetResponseDTO(
        Long id,
        String nome,
        String raca,
        String especie,
        LocalDate dataDeNascimento,
        Porte porte,
        Double peso,
        Sexo sexo,
        String codigoVinculo
) {
    public PetResponseDTO(Pet pet){
        this(pet.getIdPet(), pet.getNome(), pet.getRaca(), pet.getEspecie(),pet.getDataDeNascimento(), pet.getPorte(), pet.getPeso(), pet.getSexo(),
                pet.getCodigoVinculo());
    }
}
