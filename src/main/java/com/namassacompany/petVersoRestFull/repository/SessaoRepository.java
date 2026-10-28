package com.namassacompany.petVersoRestFull.repository;

import com.namassacompany.petVersoRestFull.model.Sessao;
import com.namassacompany.petVersoRestFull.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessaoRepository extends JpaRepository<Sessao, Long> {

    void deleteByToken(String token);
    void deleteByUsuarioAndTokenNot(Usuario usuario, String token);

    Optional<Sessao> findByToken(String token);
}
