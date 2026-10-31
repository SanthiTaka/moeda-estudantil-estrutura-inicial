
package br.edu.pucminas.moedaestudantil.infrastructure.email;

public record CupomResgatadoEvent(
    String emailAluno,
    String nomeAluno,
    String emailEmpresa,
    String nomeEmpresa,
    String descricaoVantagem,
    String codigoCupom,
    int custoMoedas
) {}
