
package br.edu.pucminas.moedaestudantil.dto.response;

import java.time.LocalDateTime;

public record ResgateResponse(
    Long id,
    Long vantagemId,
    String descricao,
    String empresaNome,
    Integer custoMoedas,
    String codigoCupom,
    LocalDateTime data
) {}
