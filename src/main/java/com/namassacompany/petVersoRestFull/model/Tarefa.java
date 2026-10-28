package com.namassacompany.petVersoRestFull.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class Tarefa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTarefa;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(nullable = false, name = "id_pet")
    private Pet pet;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "descricao")
    private String descricao;

    @Column(nullable = false, name = "data_tarefa")
    private LocalDate dataTarefa;

    @Column(name = "hora")
    private LocalTime hora;

    @JoinColumn(name = "criado_por", nullable = false)
    @ManyToOne(fetch = LAZY)
    private Usuario criadaPor;

    @Column(name = "concluida", nullable = false)
    private boolean concluida;

    @JoinColumn(name = "executado_por", nullable = true)
    @ManyToOne(fetch = LAZY)
    private Usuario executadoPor;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    void aoCriar() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }

    public Tarefa() {
    }

    public Tarefa(Pet pet, String titulo, String descricao, LocalDate dataTarefa, LocalTime hora, Usuario criadaPor) {
        this.pet = pet;
        this.titulo = titulo;
        this.descricao = descricao;
        this.dataTarefa = dataTarefa;
        this.hora = hora;
        this.criadaPor = criadaPor;
    }

    public Usuario getCriadaPor() {
        return criadaPor;
    }

    public void setCriadaPor(Usuario criadaPor) {
        this.criadaPor = criadaPor;
    }

    public Long getIdTarefa() {
        return idTarefa;
    }

    public void setIdTarefa(Long idTarefa) {
        this.idTarefa = idTarefa;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getDataTarefa() {
        return dataTarefa;
    }

    public void setDataTarefa(LocalDate dataTarefa) {
        this.dataTarefa = dataTarefa;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public boolean isConcluida() {
        return concluida;
    }

    public void setConcluida(boolean concluida) {
        this.concluida = concluida;
    }

    public LocalDateTime getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(LocalDateTime dataConclusao) {
        this.dataConclusao = dataConclusao;
    }

    public Usuario getExecutadoPor() {
        return executadoPor;
    }

    public void setExecutadoPor(Usuario executadoPor) {
        this.executadoPor = executadoPor;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Tarefa tarefa = (Tarefa) o;
        return Objects.equals(idTarefa, tarefa.idTarefa);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idTarefa);
    }
}
