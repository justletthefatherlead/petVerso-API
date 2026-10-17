package com.namassacompany.petVersoRestFull.service;

import com.namassacompany.petVersoRestFull.exception.AcessoNegadoException;
import com.namassacompany.petVersoRestFull.model.*;
import com.namassacompany.petVersoRestFull.repository.VinculoPetRepository;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class VinculoAcessoService {
    private final VinculoPetRepository vinculoPetRepository;

    public VinculoAcessoService(VinculoPetRepository vinculoPetRepository){
        this.vinculoPetRepository = vinculoPetRepository;
    }

    public VinculoPet buscarVinculoAceito(Pet pet, Usuario usuario, Supplier<? extends RuntimeException> erro) {
        VinculoPet vinculo = vinculoPetRepository.findByPetAndUsuario(pet, usuario).orElseThrow(erro);
        if (vinculo.getStatus() != StatusDeVinculo.ACEITO) {
            throw erro.get();
        }
        return vinculo;
    }
    public VinculoPet buscarVinculoDono(Pet pet, Usuario usuario, Supplier<? extends RuntimeException> erro){
         VinculoPet vinculo = buscarVinculoAceito(pet, usuario, erro);
        if (vinculo.getPapel() != Papel.DONO){
            throw new AcessoNegadoException("Voce nao tem permissao para realizar esta acao");
        }
        return vinculo;
    }
}
