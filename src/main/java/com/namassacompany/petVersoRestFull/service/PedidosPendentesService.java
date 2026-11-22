package com.namassacompany.petVersoRestFull.service;

import com.namassacompany.petVersoRestFull.dto.PedidosPendentesResponseDTO;
import com.namassacompany.petVersoRestFull.dto.SolicitacaoResponseDTO;
import com.namassacompany.petVersoRestFull.dto.SolicitarAlteracaoResponseDTO;
import com.namassacompany.petVersoRestFull.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidosPendentesService {

    private final PetService petService;
    private final SolicitacaoAlteracaoService solicitacaoAlteracaoService;

    public PedidosPendentesService(PetService petService, SolicitacaoAlteracaoService solicitacaoAlteracaoService) {
        this.petService = petService;
        this.solicitacaoAlteracaoService = solicitacaoAlteracaoService;

    }
    @Transactional(readOnly = true)
    public PedidosPendentesResponseDTO listar(Usuario usuario){
        List<SolicitacaoResponseDTO> vinculos = petService.listarSolicitacoes(usuario);
        List<SolicitarAlteracaoResponseDTO> alteracoes = solicitacaoAlteracaoService.listarPendentes(usuario);

        return new PedidosPendentesResponseDTO(vinculos,alteracoes);

    }
}
