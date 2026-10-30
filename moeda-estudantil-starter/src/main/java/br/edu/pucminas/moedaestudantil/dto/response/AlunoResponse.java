
package br.edu.pucminas.moedaestudantil.dto.response;

public record AlunoResponse(
    Long id,
    String nome,
    String email,
    String login,
    String cpf,
    String rg,
    String endereco,
    String curso,
    Long instituicaoId,
    String instituicaoNome
) {}
