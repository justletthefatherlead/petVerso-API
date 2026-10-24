package com.namassacompany.petVersoRestFull.controller;
import com.namassacompany.petVersoRestFull.dto.LoginRequestDTO;
import com.namassacompany.petVersoRestFull.dto.TokenResponseDTO;
import com.namassacompany.petVersoRestFull.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
        @PostMapping("/api/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto){
    TokenResponseDTO novoLogin = authService.login(dto);
    return ResponseEntity.status(HttpStatus.OK).body(novoLogin);
}
}
