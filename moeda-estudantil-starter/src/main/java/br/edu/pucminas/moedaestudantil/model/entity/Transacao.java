package br.edu.pucminas.moedaestudantil.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes")
public class Transacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    @Column(nullable = false)
    private Integer valor;

    @Column(nullable = false)
    private String tipo;

    @Column
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime data;

    public Transacao() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Conta getConta() { return conta; }
    public void setConta(Conta conta) { this.conta = conta; }

    public Integer getValor() { return valor; }
    public void setValor(Integer valor) { this.valor = valor; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }
    @Column(name = "periodo_semestre", length = 8)
private String periodoSemestre;
public String getPeriodoSemestre() {
    return periodoSemestre;
}

public void setPeriodoSemestre(String periodoSemestre) {
    this.periodoSemestre = periodoSemestre;
}
@Column(name = "operacao_id", length = 36)
private String operacaoId;
public String getOperacaoId() {
    return operacaoId;
}

public void setOperacaoId(String operacaoId) {
    this.operacaoId = operacaoId;
}
}
