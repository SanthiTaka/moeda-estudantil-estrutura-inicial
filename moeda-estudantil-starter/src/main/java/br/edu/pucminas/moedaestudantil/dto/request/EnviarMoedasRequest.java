package br.edu.pucminas.moedaestudantil.dto.request;

/** DTO inicial: ajustar validacoes e campos na implementacao. */
public record EnviarMoedasRequest(Long alunoId, Integer quantidade, String motivo) {}
