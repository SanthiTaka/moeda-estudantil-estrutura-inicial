
package br.edu.pucminas.moedaestudantil.dto.response;

import java.time.LocalDateTime;

public record TransacaoResponse(
    Long id,
    LocalDateTime data,
    String tipo,
    Integer valor,
    String motivo
) {}
