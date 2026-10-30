
package br.edu.pucminas.moedaestudantil.dto.response;

public record EmpresaParceiraResponse(
    Long id,
    String nome,
    String email,
    String login,
    String cnpj
) {}
