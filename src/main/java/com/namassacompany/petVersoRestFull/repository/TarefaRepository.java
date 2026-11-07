package com.namassacompany.petVersoRestFull.repository;

import com.namassacompany.petVersoRestFull.model.Pet;
import com.namassacompany.petVersoRestFull.model.StatusDeVinculo;
import com.namassacompany.petVersoRestFull.model.Tarefa;
import com.namassacompany.petVersoRestFull.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
    List<Tarefa> findByPetIdPetOrderByDataTarefaAscHoraAsc(Long idPet);
    @Query("""
           SELECT t FROM Tarefa t, VinculoPet v
           WHERE v.pet = t.pet
           AND v.usuario = :usuario
           AND v.status = :status
           AND t.dataTarefa = :data
           ORDER BY t.hora
           """)
   List<Tarefa> listarDoDia(@Param("usuario") Usuario usuario,
                            @Param("status") StatusDeVinculo status,
                            @Param("data")LocalDate data
    );
    void deleteByPet(Pet pet);
}
