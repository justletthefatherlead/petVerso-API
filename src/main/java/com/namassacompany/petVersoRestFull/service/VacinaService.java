package com.namassacompany.petVersoRestFull.service;

import com.namassacompany.petVersoRestFull.dto.VacinaRequestDTO;
import com.namassacompany.petVersoRestFull.dto.VacinaResponseDTO;
import com.namassacompany.petVersoRestFull.exception.PetNaoEncontradoException;
import com.namassacompany.petVersoRestFull.model.Pet;
import com.namassacompany.petVersoRestFull.model.Usuario;
import com.namassacompany.petVersoRestFull.repository.PetRepository;
import com.namassacompany.petVersoRestFull.repository.VacinaRepository;
import com.namassacompany.petVersoRestFull.repository.VinculoPetRepository;
import org.springframework.stereotype.Service;

@Service
public class VacinaService {
    private final VacinaRepository vacinaRepository;
    private final PetRepository petRepository;
    private final VinculoPetRepository vinculoPetRepository;

    public VacinaService(VacinaRepository vacinaRepository, PetRepository petRepository, VinculoPetRepository vinculoPetRepository) {
        this.vacinaRepository = vacinaRepository;
        this.petRepository = petRepository;
        this.vinculoPetRepository = vinculoPetRepository;
    }

    public VacinaResponseDTO cadastrar(Long idPet, VacinaRequestDTO dto, Usuario usuario){
        Pet pet = petRepository.findById(idPet).orElseThrow(()-> new PetNaoEncontradoException("Pet nao encontrado"));

    }


}
