package com.namassacompany.petVersoRestFull.service;

import com.namassacompany.petVersoRestFull.dto.*;
import com.namassacompany.petVersoRestFull.exception.DadosInvalidosException;
import com.namassacompany.petVersoRestFull.exception.EmailJaCadastradoException;
import com.namassacompany.petVersoRestFull.exception.TelefoneJaCadastradoException;
import com.namassacompany.petVersoRestFull.model.Usuario;
import com.namassacompany.petVersoRestFull.repository.SessaoRepository;
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
    private final SessaoRepository sessaoRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, AuthService authService, SessaoRepository sessaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.sessaoRepository = sessaoRepository;
    }

    @Transactional
    public UsuarioCadastroResponseDTO cadastrar(UsuarioRequestDTO dto) {

        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new EmailJaCadastradoException("o email " + dto.email() + " ja esta cadastrado.");
        }
        if (usuarioRepository.existsByTelefone(dto.telefone())) {
            throw new TelefoneJaCadastradoException("o telefone " + dto.telefone() + " já foi cadastrado.");
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

    public UsuarioPerfilResponseDTO atualizarPerfil(UsuarioPerfilRequestDTO dto, Usuario usuario) {
        if (dto.apelido() != null) {
            usuario.setApelido(dto.apelido());
        }
        if (dto.fotoBase64() != null) {
            usuario.setFoto(Base64.getDecoder().decode(dto.fotoBase64()));
        }
        Usuario usuariosalvo = usuarioRepository.save(usuario);

        return new UsuarioPerfilResponseDTO(usuariosalvo);
    }

    @Transactional
    public UsuarioResponseDTO alterarDados(Usuario autenticado, UsuarioDadosRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(autenticado.getIdUsuario()).orElseThrow(() -> new DadosInvalidosException("Usuario nao encontrado"));
        String telefone = usuario.getTelefone();
        if (usuarioRepository.existsByTelefoneAndUsuario(telefone, usuario.getIdUsuario())) {
            throw new TelefoneJaCadastradoException("Telefone ja cadastrado");
        }
        usuario.setNome(dto.nome());
        usuario.setTelefone(dto.telefone());
        return new UsuarioResponseDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public void alterarSenha(Usuario autenticado, String tokenAtual, UsuarioSenhaRequestDTO senha) {
        Usuario usuario = usuarioRepository
                .findById(autenticado.getIdUsuario()).orElseThrow(() -> new DadosInvalidosException("Usuario nao encontrado"));

        if (!passwordEncoder.matches(senha.senhaAtual(), usuario.getSenha())){
            throw new DadosInvalidosException("Senha atual incorreta");
        }
        if (passwordEncoder.matches(senha.novaSenha(), usuario.getSenha())){
            throw new DadosInvalidosException("A nova senha deve ser diferente da senha atual");
        }

        usuario.setSenha(passwordEncoder.encode(senha.novaSenha()));
        usuarioRepository.save(usuario);
        sessaoRepository.deleteUsuarioAndTokenNot(usuario,tokenAtual);

    }

    public UsuarioPerfilResponseDTO vizualizarPerfil(Usuario usuario) {
        return new UsuarioPerfilResponseDTO(usuario);
    }

}
