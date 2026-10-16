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

    @Column(name = "dataAplicacao", nullable = false)
    private LocalDate dataAplicacao;

    @Column(name = "dataProximaDose")
    private LocalDate dataProximaDose;

    @Column(name = "observacao", length = 500)
    private String observacao;

   protected Vacina(){

   }
    public Vacina(Pet pet, String nome, LocalDate dataAplicacao, LocalDate dataProximaDose, String observacao) {
        this.pet = pet;
        this.nome = nome;
        this.dataAplicacao = dataAplicacao;
        this.dataProximaDose = dataProximaDose;
        this.observacao = observacao;
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
