package com.namassacompany.petVersoRestFull.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Vacina {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVacina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Pet pet;

    @Column(nullable = false, name = "nome")
    private String nome;

    @Column(name = "dataAplicacao")
    private LocalDate dataAplicacao;

    @Column
    private boolean dosagemUnica;

    @Column
    private Integer intervaloQuantidade;

    @Column
    @Enumerated(EnumType.STRING)
    private IntervaloUnidade intervaloUnidade;

    @Column
    private LocalDate dataProximaDose;

    @Column(name = "observacao", length = 500)
    private String observacao;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusVacina status;


    protected Vacina() {

    }

    public StatusVacina getStatus() {
        return status;
    }

    public void setStatus(StatusVacina status) {
        this.status = status;
    }

    public IntervaloUnidade getIntervaloUnidade() {
        return intervaloUnidade;
    }

    public void setIntervaloUnidade(IntervaloUnidade intervaloUnidade) {
        this.intervaloUnidade = intervaloUnidade;
    }

    public Integer getIntervaloQuantidade() {
        return intervaloQuantidade;
    }

    public void setIntervaloQuantidade(Integer intervaloQuantidade) {
        this.intervaloQuantidade = intervaloQuantidade;
    }

    public boolean isDosagemUnica() {
        return dosagemUnica;
    }

    public void setDosagemUnica(boolean dosagemUnica) {
        this.dosagemUnica = dosagemUnica;
    }

    public Vacina( Pet pet, String nome, LocalDate dataAplicacao, boolean dosagemUnica, Integer intervaloQuantidade, IntervaloUnidade intervaloUnidade, LocalDate dataProximaDose, String observacao, StatusVacina status) {
        this.pet = pet;
        this.nome = nome;
        this.dataAplicacao = dataAplicacao;
        this.dosagemUnica = dosagemUnica;
        this.intervaloQuantidade = intervaloQuantidade;
        this.intervaloUnidade = intervaloUnidade;
        this.dataProximaDose = dataProximaDose;
        this.observacao = observacao;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Vacina vacina = (Vacina) o;
        return Objects.equals(idVacina, vacina.idVacina);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idVacina);
    }

    public Long getIdVacina() {
        return idVacina;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataAplicacao() {
        return dataAplicacao;
    }

    public void setDataAplicacao(LocalDate dataAplicacao) {
        this.dataAplicacao = dataAplicacao;
    }

    public LocalDate getDataProximaDose() {
        return dataProximaDose;
    }

    public void setDataProximaDose(LocalDate dataProximaDose) {
        this.dataProximaDose = dataProximaDose;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
