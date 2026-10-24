package com.namassacompany.petVersoRestFull.dto;

import com.namassacompany.petVersoRestFull.model.Papel;
import com.namassacompany.petVersoRestFull.model.Pet;
import com.namassacompany.petVersoRestFull.model.Porte;
import com.namassacompany.petVersoRestFull.model.Sexo;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

public record PetPerfilResponseDTO(
        Long id,
        String nome,
        String raca,
        String especie,
        LocalDate idade,
        Porte porte,
        Double peso,
        Sexo sexo,
        String fotoPetBase64,
        String codigoVinculo,
        String perfilDeSensibilidade,
        List<String> personalidades
) {
    public PetPerfilResponseDTO(Pet pet, Papel papel){
        this(pet.getIdPet(), pet.getNome(), pet.getRaca(), pet.getEspecie(),pet.getDataDeNascimento(), pet.getPorte(), pet.getPeso(), pet.getSexo(),
                (pet.getFoto() != null) ? Base64.getEncoder().encodeToString(pet.getFoto()): null,
                (papel == Papel.DONO)? pet.getCodigoVinculo() : null,
                pet.getPerfilSensibilidade(),
                List.copyOf(pet.getPersonalidades()));
    }
}
