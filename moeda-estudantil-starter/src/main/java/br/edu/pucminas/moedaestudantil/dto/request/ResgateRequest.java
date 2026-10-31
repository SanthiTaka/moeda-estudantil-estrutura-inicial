
package br.edu.pucminas.moedaestudantil.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ResgateRequest(

    @NotNull(message = "Selecione uma vantagem.")
    @Positive(message = "A vantagem informada e invalida.")
    Long vantagemId

) {}
