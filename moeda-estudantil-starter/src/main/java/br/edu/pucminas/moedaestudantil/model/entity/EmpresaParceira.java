package br.edu.pucminas.moedaestudantil.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "empresas")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class EmpresaParceira extends Usuario {
    @Column(nullable = false, unique = true)
    private String cnpj;

    public EmpresaParceira() {}

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
}
