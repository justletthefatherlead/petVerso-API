package com.namassacompany.petVersoRestFull.service;

import com.namassacompany.petVersoRestFull.dto.LoginRequestDTO;
import com.namassacompany.petVersoRestFull.dto.TokenResponseDTO;
import com.namassacompany.petVersoRestFull.exception.CredenciaisInvalidasException;
import com.namassacompany.petVersoRestFull.model.Sessao;
import com.namassacompany.petVersoRestFull.model.Usuario;
import com.namassacompany.petVersoRestFull.repository.SessaoRepository;
import com.namassacompany.petVersoRestFull.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {
    private final SessaoRepository sessaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, SessaoRepository sessaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessaoRepository = sessaoRepository;
    }

    public TokenResponseDTO login(LoginRequestDTO dto) {
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByEmail(dto.login());
        if (usuarioEncontrado.isEmpty()) {
            throw new CredenciaisInvalidasException("Email ou senha invalidos");
        }
        Usuario usuario = usuarioEncontrado.get();

        boolean senhaCorreta = passwordEncoder.matches(dto.senha(), usuario.getSenha());
        if (!senhaCorreta) {
            throw new CredenciaisInvalidasException("Email ou senha invalidos");
        }
        return iniciarSessao(usuario);

    }

    public TokenResponseDTO iniciarSessao(Usuario usuario) {
        String token = gerarToken();

        Sessao sessao = new Sessao(
                null, token, usuario, LocalDateTime.now()
        );
        sessaoRepository.save(sessao);
        return new TokenResponseDTO(usuario.getNome(), token);
    }

    public String gerarToken() {
        SecureRandom secureRandom = new SecureRandom();
        int bound = 1000000;
        int numberRandom = secureRandom.nextInt(0, bound);

        return String.format("%06d", numberRandom);
    }
}
