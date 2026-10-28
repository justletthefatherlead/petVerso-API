package com.namassacompany.petVersoRestFull.repository;

import com.namassacompany.petVersoRestFull.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.time.ZoneId;

public interface SolicitacaoAlteracaoRepository extends JpaRepository<SolicitacaoAlteracao, Long> {

    void deleteByTipoAndIdAlvo(TipoAlteracao tipo, Long idAlvo);

    @Modifying(clearAutomatically = true)
    @Query(
            "update SolicitacaoAlteracao s set s.status = :recusada, s.dataResposta = :agora " +
                    "where s.pet = :pet and s.solicitante = :usuario and s.status = :pendente")
    int cancelarPendentes(@Param("pet") Pet pet, @Param("usuario") Usuario usuario,
                          @Param("pendente")StatusSolicitacao pendente,
                          @Param("recusada")StatusSolicitacao recusada,
                          @Param("agora")LocalDateTime agora);
    default void cancelarPendentes(Pet pet, Usuario usuario){
        cancelarPendentes(pet, usuario,
                StatusSolicitacao.PENDENTE,
                StatusSolicitacao.RECUSADA,
                LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
    }
}
