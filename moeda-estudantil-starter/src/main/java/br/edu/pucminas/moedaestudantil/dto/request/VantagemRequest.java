
package br.edu.pucminas.moedaestudantil.dto.request;

import jakarta.validation.constraints.*;

public record VantagemRequest(

    @NotBlank(message = "Informe a descricao.")
    @Size(min = 5, max = 255)
    String descricao,

    @NotBlank(message = "Informe a foto.")
    @Size(max = 255)
    @Pattern(
        regexp = "https://[^\\s]+",
        message = "Informe uma URL HTTPS de imagem."
    )
    String foto,

    @NotNull
    @Min(value = 1, message = "O custo minimo e 1 moeda.")
    @Max(value = 1000000)
    Integer custoMoedas

) {}
