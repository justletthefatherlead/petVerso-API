package com.namassacompany.petVersoRestFull.service;

import com.namassacompany.petVersoRestFull.dto.VacinaRequestDTO;
import com.namassacompany.petVersoRestFull.dto.VacinaResponseDTO;
import com.namassacompany.petVersoRestFull.exception.DadosInvalidosException;
import com.namassacompany.petVersoRestFull.exception.PetNaoEncontradoException;
import com.namassacompany.petVersoRestFull.exception.VacinaNaoEncontradaException;
import com.namassacompany.petVersoRestFull.model.*;
import com.namassacompany.petVersoRestFull.repository.PetRepository;
import com.namassacompany.petVersoRestFull.repository.SolicitacaoAlteracaoRepository;
import com.namassacompany.petVersoRestFull.repository.VacinaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class VacinaService {
    private final VacinaRepository vacinaRepository;
    private final PetRepository petRepository;
    private final VinculoAcessoService vinculoAcessoService;
    private final SolicitacaoAlteracaoRepository solicitacaoAlteracaoRepository;

    private static final ZoneId  ZONA = ZoneId.of("America/Sao_Paulo");
    public VacinaService(VacinaRepository vacinaRepository, PetRepository petRepository, VinculoAcessoService vinculoAcessoService, SolicitacaoAlteracaoRepository solicitacaoAlteracaoRepository) {
        this.vacinaRepository = vacinaRepository;
        this.petRepository = petRepository;
        this.vinculoAcessoService = vinculoAcessoService;
        this.solicitacaoAlteracaoRepository = solicitacaoAlteracaoRepository;
    }

    @Transactional
    public VacinaResponseDTO cadastrar(Long idPet, VacinaRequestDTO dto, Usuario usuario) {
        Pet pet = petRepository.findById(idPet).orElseThrow(() -> new PetNaoEncontradoException("Pet nao encontrado"));
        vinculoAcessoService.buscarVinculoDono(pet, usuario, () -> new PetNaoEncontradoException("Pet nao encontrado"));
        dto.validarRegras();
        boolean aplicada = dto.jaAplicada();


        Vacina vacina = new Vacina(
                pet,
                dto.nome(),
                dto.dataAplicacao(),
                dto.dosagemUnica(),
                dto.intervaloQuantidade(),
                dto.intervaloUnidade(),
                null,
                dto.observacao(),
                aplicada ? StatusVacina.APLICADA : StatusVacina.PENDENTE
        );
        recalcularProximaDose(vacina);
        return new VacinaResponseDTO(vacinaRepository.save(vacina));
    }

    @Transactional(readOnly = true)
    public List<VacinaResponseDTO> listar(Long idPet, Usuario usuario) {
        Pet pet = petRepository.findById(idPet).orElseThrow(() -> new PetNaoEncontradoException("Pet nao encontrado"));
        vinculoAcessoService.buscarVinculoAceito(pet, usuario, () -> new PetNaoEncontradoException("Pet nao encontrado"));

        return vacinaRepository.findByPetOrderByDataAplicacaoDescIdVacinaDesc(pet)
                .stream()
                .map(VacinaResponseDTO::new).toList();


    }

    @Transactional
    public VacinaResponseDTO atualizar(Long idVacina, VacinaRequestDTO dto, Usuario usuario) {
        Vacina vacina = vacinaRepository.findById(idVacina).orElseThrow(() -> new VacinaNaoEncontradaException("Vacina nao encontrada"));
        vinculoAcessoService.buscarVinculoDono(vacina.getPet(), usuario, () -> new VacinaNaoEncontradaException("Vacina nao encontrada"));
       validarAtualizacao(vacina, dto);


        vacina.setNome(dto.nome());
        vacina.setDataAplicacao(dto.dataAplicacao());
        vacina.setDosagemUnica(dto.dosagemUnica());
        vacina.setIntervaloQuantidade(dto.intervaloQuantidade());
        vacina.setIntervaloUnidade(dto.intervaloUnidade());
        vacina.setObservacao(dto.observacao());
        vacina.setStatus(dto.jaAplicada() ? StatusVacina.APLICADA : StatusVacina.PENDENTE);
       recalcularProximaDose(vacina);


        return new VacinaResponseDTO(vacina);

    }

    @Transactional
    public VacinaResponseDTO marcarComoAplicada(Long idVacina, Usuario usuario){
         Vacina vacina = vacinaRepository.findById(idVacina).orElseThrow(() -> new VacinaNaoEncontradaException("Vacina nao encontrada"));
        vinculoAcessoService.buscarVinculoAceito(vacina.getPet(), usuario, () -> new VacinaNaoEncontradaException("Vacina nao encontrada"));
        if (vacina.getStatus() == StatusVacina.APLICADA) throw new IllegalStateException("Essa vacina ja foi aplicada");
        vacina.setStatus(StatusVacina.APLICADA);
        vacina.setDataAplicacao(LocalDate.now(ZONA));
        recalcularProximaDose(vacina);
        return new VacinaResponseDTO(vacina);
    }

    @Transactional
    public void excluir(Long idVacina , Usuario usuario){
        Vacina vacina = vacinaRepository.findById(idVacina).orElseThrow(()-> new VacinaNaoEncontradaException("Vacina nao encontrada"));
        vinculoAcessoService.buscarVinculoDono(vacina.getPet(), usuario, ()->new VacinaNaoEncontradaException("Vacina nao encontrada"));
        solicitacaoAlteracaoRepository.deleteByTipoAndIdAlvo(TipoAlteracao.ALTERAR_VACINA, idVacina);
        vacinaRepository.delete(vacina);
    }

    public void validarAtualizacao(Vacina vacina, VacinaRequestDTO dto) {
        dto.validarRegras();
        if (vacina.getStatus() == StatusVacina.APLICADA && !dto.jaAplicada()) {
            throw new DadosInvalidosException("Vacina ja aplicada nao pode voltar a pendente");
        }
    }



    private LocalDate calcularProximaDose(LocalDate base, Integer quantidade, IntervaloUnidade unidade) {
        return switch (unidade) {
            case DIAS -> base.plusDays(quantidade);
            case SEMANAS -> base.plusWeeks(quantidade);
            case MESES -> base.plusMonths(quantidade);
            case ANOS -> base.plusYears(quantidade);
        };
    }
    private void recalcularProximaDose(Vacina vacina){
        if (vacina.getStatus() == StatusVacina.APLICADA && !vacina.isDosagemUnica()) {
            vacina.setDataProximaDose(calcularProximaDose(vacina.getDataAplicacao(), vacina.getIntervaloQuantidade(), vacina.getIntervaloUnidade()));
            return;
        }
        vacina.setDataProximaDose(null);
    }


}
