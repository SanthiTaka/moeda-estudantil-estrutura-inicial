
package br.edu.pucminas.moedaestudantil.dto.response;

import java.util.List;

public record ExtratoResponse(
    Integer saldo,
    List<TransacaoResponse> transacoes
) {}
