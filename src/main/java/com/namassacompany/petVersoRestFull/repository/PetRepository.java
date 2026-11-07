package com.namassacompany.petVersoRestFull.repository;

import com.namassacompany.petVersoRestFull.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    Optional<Pet> findByCodigoVinculo(String codigoVinculo);


}
