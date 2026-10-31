
package br.edu.pucminas.moedaestudantil.dto.response;

public record VantagemResponse(
    Long id,
    String descricao,
    String foto,
    Integer custoMoedas,
    Long empresaId,
    String empresaNome
) {}
