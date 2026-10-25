package com.namassacompany.petVersoRestFull.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
public class SolicitacaoAlteracao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSolicitacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pet", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitante", nullable = false)
    private Usuario solicitante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlteracao tipo;

    @Column(nullable = false)
    private Long idAlvo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String dadosPropostos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacao status;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    private LocalDateTime dataResposta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondido_por")
    private Usuario respondidoPor;

    private String motivoRecusa;


    public SolicitacaoAlteracao(Pet pet, Usuario solicitante, TipoAlteracao tipo,
                                Long idAlvo, String dadosPropostos) {
        this.pet = pet;
        this.solicitante = solicitante;
        this.tipo = tipo;
        this.idAlvo = idAlvo;
        this.dadosPropostos = dadosPropostos;
        this.status = StatusSolicitacao.PENDENTE;
        this.dataCriacao = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
    }

    public SolicitacaoAlteracao() {
    }

    public Long getIdSolicitacao() {
        return idSolicitacao;
    }

    public LocalDateTime getDataResposta() {
        return dataResposta;
    }

    public void setDataResposta(LocalDateTime dataResposta) {
        this.dataResposta = dataResposta;
    }

    public String getMotivoRecusa() {
        return motivoRecusa;
    }

    public void setMotivoRecusa(String motivoRecusa) {
        this.motivoRecusa = motivoRecusa;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Usuario solicitante) {
        this.solicitante = solicitante;
    }

    public TipoAlteracao getTipo() {
        return tipo;
    }

    public void setTipo(TipoAlteracao tipo) {
        this.tipo = tipo;
    }

    public Long getIdAlvo() {
        return idAlvo;
    }

    public void setIdAlvo(Long idAlvo) {
        this.idAlvo = idAlvo;
    }

    public String getDadosPropostos() {
        return dadosPropostos;
    }

    public void setDadosPropostos(String dadosPropostos) {
        this.dadosPropostos = dadosPropostos;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacao status) {
        this.status = status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Usuario getRespondidoPor() {
        return respondidoPor;
    }

    public void setRespondidoPor(Usuario respondidoPor) {
        this.respondidoPor = respondidoPor;
    }
}

