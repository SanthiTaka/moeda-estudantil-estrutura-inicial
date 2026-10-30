package br.edu.pucminas.moedaestudantil.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vantagens")
public class Vantagem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaParceira empresa;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private String foto;

    @Column(name = "custo_moedas", nullable = false)
    private Integer custoMoedas;

    public Vantagem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EmpresaParceira getEmpresa() { return empresa; }
    public void setEmpresa(EmpresaParceira empresa) { this.empresa = empresa; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public Integer getCustoMoedas() { return custoMoedas; }
    public void setCustoMoedas(Integer custoMoedas) { this.custoMoedas = custoMoedas; }
}
