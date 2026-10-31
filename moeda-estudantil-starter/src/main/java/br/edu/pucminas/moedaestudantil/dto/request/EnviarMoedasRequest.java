
package br.edu.pucminas.moedaestudantil.dto.request;

import jakarta.validation.constraints.*;

public record EnviarMoedasRequest(

    @NotNull
    Long alunoId,

    @NotNull
    @Positive
    Integer quantidade,

    @NotBlank
    @Size(max = 500)
    String motivo

) {}
