package com.namassacompany.petVersoRestFull.repository;

import com.namassacompany.petVersoRestFull.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {
    boolean existsByEmail(String email);
    boolean existsByTelefone(String telefone);
    boolean existsByTelefoneAndUsuario(String telefone, Long idUsuario);

    Optional<Usuario> findByEmail(String email);
}
