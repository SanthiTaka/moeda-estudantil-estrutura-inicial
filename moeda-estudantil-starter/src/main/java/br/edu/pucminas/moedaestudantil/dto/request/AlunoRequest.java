
package br.edu.pucminas.moedaestudantil.dto.request;

import jakarta.validation.constraints.*;

public record AlunoRequest(
    @NotBlank @Size(max = 120) String nome,
    @NotBlank @Email @Size(max = 160) String email,
    @NotBlank @Size(min = 3, max = 60) String login,
    String senha,
    @NotBlank @Pattern(regexp = "\\d{11}") String cpf,
    @NotBlank @Size(min = 5, max = 20) String rg,
    @NotBlank @Size(max = 250) String endereco,
    @NotBlank @Size(max = 120) String curso,
    @NotNull Long instituicaoId
) {}
