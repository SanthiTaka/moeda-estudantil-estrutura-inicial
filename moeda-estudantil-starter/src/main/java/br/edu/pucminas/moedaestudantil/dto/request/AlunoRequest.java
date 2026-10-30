package br.edu.pucminas.moedaestudantil.dto.request;

/** DTO inicial: ajustar validacoes e campos na implementacao. */
public record AlunoRequest(String nome, String email, String login, String senha, String cpf, String rg, String endereco, String curso, Long instituicaoId) {}
