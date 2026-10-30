
package br.edu.pucminas.moedaestudantil.dto.request;

import jakarta.validation.constraints.*;

public record EmpresaParceiraRequest(

    @NotBlank
    @Size(max = 120)
    String nome,

    @NotBlank
    @Email
    @Size(max = 160)
    String email,

    @NotBlank
    @Size(min = 3, max = 60)
    String login,

    String senha,

    @NotBlank
    @Pattern(
        regexp = "[A-Za-z0-9]{12}[0-9]{2}",
        message = "CNPJ deve ter 12 caracteres alfanumericos e 2 digitos finais"
    )
    String cnpj

) {}
