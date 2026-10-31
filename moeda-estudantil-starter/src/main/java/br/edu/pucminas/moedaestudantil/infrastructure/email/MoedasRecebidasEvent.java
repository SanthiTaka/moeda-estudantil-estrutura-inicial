
package br.edu.pucminas.moedaestudantil.infrastructure.email;

public record MoedasRecebidasEvent(
    String emailAluno,
    String nomeAluno,
    String nomeProfessor,
    int quantidade,
    String motivo
) {}
