package com.namassacompany.petVersoRestFull.repository;

import com.namassacompany.petVersoRestFull.model.Pet;
import com.namassacompany.petVersoRestFull.model.Vacina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacinaRepository extends JpaRepository<Vacina, Long> {
    List<Vacina> findByPetOrderByDataAplicacaoDescIdVacinaDesc(Pet pet);

}
