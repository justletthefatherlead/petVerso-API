package com.namassacompany.petVersoRestFull.service;

import com.namassacompany.petVersoRestFull.dto.*;
import com.namassacompany.petVersoRestFull.exception.EmailJaCadastradoException;
import com.namassacompany.petVersoRestFull.exception.TelefoneJaCadastradoException;
import com.namassacompany.petVersoRestFull.model.Usuario;
import com.namassacompany.petVersoRestFull.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;


@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, AuthService authService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }
    @Transactional
    public UsuarioCadastroResponseDTO cadastrar(UsuarioRequestDTO dto){

        if(usuarioRepository.existsByEmail(dto.email())){
            throw new EmailJaCadastradoException("o email "+ dto.email()+" ja esta cadastrado.");
        }
        if(usuarioRepository.existsByTelefone(dto.telefone())){
            throw new TelefoneJaCadastradoException("o telefone "  + dto.telefone()+" já foi cadastrado.");
        }

        String senhaHash = passwordEncoder.encode(dto.senha());
        Usuario usuario = new Usuario(
                null,
                dto.nome(),
                null,
                dto.email(),
                dto.telefone(),
                null,
                senhaHash
        );
        Usuario usuarioSalvo = usuarioRepository.save(usuario);

         TokenResponseDTO sessao = authService.iniciarSessao(usuarioSalvo);

        return new UsuarioCadastroResponseDTO(
                usuarioSalvo.getIdUsuario(),
                usuarioSalvo.getNome(),
                usuarioSalvo.getEmail(),
                sessao.token()
        );
    }

    public UsuarioPerfilResponseDTO atualizarPerfil(UsuarioPerfilRequestDTO dto, Usuario usuario){
        if(dto.apelido()!= null ){
            usuario.setApelido(dto.apelido());
        }
        if (dto.fotoBase64() != null){
            usuario.setFoto(Base64.getDecoder().decode(dto.fotoBase64()));
        }
        Usuario usuariosalvo = usuarioRepository.save(usuario);

        return new UsuarioPerfilResponseDTO(usuariosalvo);
    }

    public UsuarioPerfilResponseDTO vizualizarPerfil(Usuario usuario){
        return new UsuarioPerfilResponseDTO(usuario);
    }

}
