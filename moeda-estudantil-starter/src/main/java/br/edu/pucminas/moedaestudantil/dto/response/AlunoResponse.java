package br.edu.pucminas.moedaestudantil.dto.response;

/** DTO inicial: ajustar validacoes e campos na implementacao. */
public record AlunoResponse(Long id, String nome, String email, String cpf, String rg, String endereco, String curso, String instituicao) {}
