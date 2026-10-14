package com.namassacompany.petVersoRestFull.repository;

import com.namassacompany.petVersoRestFull.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VinculoPetRepository extends JpaRepository<VinculoPet, Long> {

        boolean existsByPetAndUsuario(Pet pet, Usuario usuario);

    Optional<VinculoPet> findByPetAndUsuario(Pet pet, Usuario usuario);

    List<VinculoPet> findByUsuarioAndStatus(Usuario usuario, StatusDeVinculo statusDeVinculo);
    List<VinculoPet> findByPetAndStatus(Pet pet, StatusDeVinculo status);
    List<VinculoPet> findByUsuarioAndPapel(Usuario usuario, Papel papel);
}
