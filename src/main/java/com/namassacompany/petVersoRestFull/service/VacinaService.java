package com.namassacompany.petVersoRestFull.service;

import com.namassacompany.petVersoRestFull.dto.VacinaRequestDTO;
import com.namassacompany.petVersoRestFull.dto.VacinaResponseDTO;
import com.namassacompany.petVersoRestFull.exception.DadosInvalidosException;
import com.namassacompany.petVersoRestFull.exception.PetNaoEncontradoException;
import com.namassacompany.petVersoRestFull.model.Pet;
import com.namassacompany.petVersoRestFull.model.Usuario;
import com.namassacompany.petVersoRestFull.model.Vacina;
import com.namassacompany.petVersoRestFull.repository.PetRepository;
import com.namassacompany.petVersoRestFull.repository.VacinaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class VacinaService {
    private final VacinaRepository vacinaRepository;
    private final PetRepository petRepository;
    private final VinculoAcessoService vinculoAcessoService;

    public VacinaService(VacinaRepository vacinaRepository, PetRepository petRepository, VinculoAcessoService vinculoAcessoService) {
        this.vacinaRepository = vacinaRepository;
        this.petRepository = petRepository;
        this.vinculoAcessoService = vinculoAcessoService;
    }

    @Transactional
    public VacinaResponseDTO cadastrar(Long idPet, VacinaRequestDTO dto, Usuario usuario) {
        Pet pet = petRepository.findById(idPet).orElseThrow(() -> new PetNaoEncontradoException("Pet nao encontrado"));
        vinculoAcessoService.buscarVinculoDono(pet, usuario, () -> new PetNaoEncontradoException("Pet nao encontrado"));
        validarDatas(dto);
        Vacina vacina = new Vacina(
                pet,
                dto.nome(),
                dto.dataAplicacao(),
                dto.dataProximaDose(),
                dto.observacao()

        );
        return new VacinaResponseDTO(vacinaRepository.save(vacina));
    }

    @Transactional(readOnly = true)
    public List<VacinaResponseDTO> listar(Long idPet, Usuario usuario) {
        Pet pet = petRepository.findById(idPet).orElseThrow(() -> new PetNaoEncontradoException("Pet nao encontrado"));
        vinculoAcessoService.buscarVinculoAceito(pet, usuario, () -> new PetNaoEncontradoException("Pet nao encontrado"));

        return vacinaRepository.findByPetOrderByDataAplicacaoDescIdVacinaDesc(pet)
                .stream()
                .map(VacinaResponseDTO::new).toList();


    }

    private void validarDatas(VacinaRequestDTO dto) {
        if (dto.dataAplicacao().isAfter(LocalDate.now()))
            throw new DadosInvalidosException("A data de aplicacao nao pode ser futura");
        if (dto.dataProximaDose() != null && dto.dataProximaDose().isBefore(dto.dataAplicacao()))
            throw new DadosInvalidosException("A data da proxima dose nao pode ser anterior a data de aplicacao");
    }


}
