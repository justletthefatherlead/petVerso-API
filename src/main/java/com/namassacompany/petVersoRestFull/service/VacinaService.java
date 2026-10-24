package com.namassacompany.petVersoRestFull.service;

import com.namassacompany.petVersoRestFull.dto.VacinaRequestDTO;
import com.namassacompany.petVersoRestFull.dto.VacinaResponseDTO;
import com.namassacompany.petVersoRestFull.exception.DadosInvalidosException;
import com.namassacompany.petVersoRestFull.exception.PetNaoEncontradoException;
import com.namassacompany.petVersoRestFull.exception.VacinaNaoEncontradaException;
import com.namassacompany.petVersoRestFull.model.*;
import com.namassacompany.petVersoRestFull.repository.PetRepository;
import com.namassacompany.petVersoRestFull.repository.VacinaRepository;
import org.springframework.http.ResponseEntity;
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
    private static final ZoneId  ZONA = ZoneId.of("America/Sao_Paulo");
    public VacinaService(VacinaRepository vacinaRepository, PetRepository petRepository, VinculoAcessoService vinculoAcessoService) {
        this.vacinaRepository = vacinaRepository;
        this.petRepository = petRepository;
        this.vinculoAcessoService = vinculoAcessoService;
    }

    @Transactional
    public VacinaResponseDTO cadastrar(Long idPet, VacinaRequestDTO dto, Usuario usuario) {
        Pet pet = petRepository.findById(idPet).orElseThrow(() -> new PetNaoEncontradoException("Pet nao encontrado"));
        vinculoAcessoService.buscarVinculoDono(pet, usuario, () -> new PetNaoEncontradoException("Pet nao encontrado"));
        validarIntervalo(dto);
        boolean aplicada = dto.jaAplicada();
        validarDataSeAplicada(dto);

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
        validarIntervalo(dto);

        boolean estavaAplicada = vacina.getStatus() == StatusVacina.APLICADA;


        if (estavaAplicada && !dto.jaAplicada()){
            throw new DadosInvalidosException(" Vacina já aplicada nao pode voltar a pendente");
        }
       validarDataSeAplicada(dto);


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

    private void validarIntervalo(VacinaRequestDTO dto) {
        if (dto.dosagemUnica()) {
            if (dto.intervaloUnidade() != null || dto.intervaloQuantidade() != null) {
                throw new DadosInvalidosException("Dose unica nao pode ter intervalos");
            }
            return;
        }
        if (dto.intervaloUnidade() == null || dto.intervaloQuantidade() == null) {
            throw new DadosInvalidosException("Informe a quantidade e a unidade do intervalo");
        }
        if (dto.intervaloQuantidade() <= 0) {
            throw new DadosInvalidosException("A quantidade do intervalo deve ser maior que zero");

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
    private void validarDataSeAplicada(VacinaRequestDTO dto){
        if (dto.jaAplicada() && dto.dataAplicacao().isAfter(LocalDate.now(ZONA))){
            throw new DadosInvalidosException("Vacina ja aplicada nao pode ter data futura");
        }
    }

}
