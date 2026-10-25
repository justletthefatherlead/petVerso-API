package com.namassacompany.petVersoRestFull.repository;

import com.namassacompany.petVersoRestFull.model.SolicitacaoAlteracao;
import com.namassacompany.petVersoRestFull.model.TipoAlteracao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoAlteracaoRepository extends JpaRepository<SolicitacaoAlteracao, Long> {

    void deleteByTipoAndIdAlvo(TipoAlteracao tipo, Long idAlvo);
}
