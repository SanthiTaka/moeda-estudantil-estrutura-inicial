
package br.edu.pucminas.moedaestudantil.dto.request;

import jakarta.validation.constraints.*;

public record EmpresaParceiraRequest(
    @NotBlank @Size(max = 120) String nome,
    @NotBlank @Email @Size(max = 160) String email,
    @NotBlank @Size(min = 3, max = 60) String login,
    String senha,
    @NotBlank @Pattern(regexp = "\\d{14}") String cnpj
) {}
