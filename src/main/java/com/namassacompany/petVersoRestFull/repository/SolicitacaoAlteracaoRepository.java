package com.namassacompany.petVersoRestFull.repository;

import com.namassacompany.petVersoRestFull.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

public interface SolicitacaoAlteracaoRepository extends JpaRepository<SolicitacaoAlteracao, Long> {

    void deleteByTipoAndIdAlvo(TipoAlteracao tipo, Long idAlvo);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            "update SolicitacaoAlteracao s set s.status = :cancelada, s.dataResposta = :agora " +
                    "where s.pet = :pet and s.solicitante = :usuario and s.status = :pendente")
    int cancelarPendentes(@Param("pet") Pet pet, @Param("usuario") Usuario usuario,
                          @Param("pendente")StatusSolicitacao pendente,
                              @Param("cancelada")StatusSolicitacao cancelada,
                          @Param("agora")LocalDateTime agora);
    default void cancelarPendentes(Pet pet, Usuario usuario){
        cancelarPendentes(pet, usuario,
                StatusSolicitacao.PENDENTE,
                StatusSolicitacao.CANCELADA,
                LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
    }

    void deleteByPet(Pet pet);

    List<SolicitacaoAlteracao> findByPetAndStatusOrderByDataCriacaoAsc(Pet pet, StatusSolicitacao status);

    Optional<SolicitacaoAlteracao> findByIdSolicitacaoAndPet(Long idSolicitacao, Pet pet);

    boolean existsBySolicitanteAndTipoAndIdAlvoAndStatus(Usuario solicitante, TipoAlteracao tipo, Long idAlvo, StatusSolicitacao status);

    @Query("select s from SolicitacaoAlteracao s " +
            "join fetch s.pet join fetch s.solicitante " +
            "where s.status = :statusSolicitacao " +
            "and s.pet in (select v.pet from VinculoPet v " +
            "where v.usuario = :usuario and v.papel = :papel and v.status = :statusVinculo) " +
            "order by s.dataCriacao asc")
    List<SolicitacaoAlteracao> listarPendentesDoDono(
            @Param("usuario") Usuario usuario,
            @Param("papel") Papel papel,
            @Param("statusVinculo") StatusDeVinculo statusVinculo,
            @Param("statusSolicitacao") StatusSolicitacao statusSolicitacao
    );

    default List<SolicitacaoAlteracao> listarPendentesDoDono(Usuario usuario){
        return listarPendentesDoDono(usuario, Papel.DONO,StatusDeVinculo.ACEITO, StatusSolicitacao.PENDENTE);
        }

}
