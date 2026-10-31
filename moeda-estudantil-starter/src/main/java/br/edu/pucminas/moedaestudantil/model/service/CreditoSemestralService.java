
package br.edu.pucminas.moedaestudantil.model.service;

import java.time.*;
import java.util.NoSuchElementException;

import br.edu.pucminas.moedaestudantil.model.entity.*;
import br.edu.pucminas.moedaestudantil.model.enums.TipoTransacao;
import br.edu.pucminas.moedaestudantil.model.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditoSemestralService {

    private static final int COTA = 1000;

    private static final ZoneId ZONA =
        ZoneId.of("America/Sao_Paulo");

    private final ProfessorRepository professores;
    private final ContaRepository contas;
    private final TransacaoRepository transacoes;
    private final ContaService contaService;

    public CreditoSemestralService(
            ProfessorRepository professores,
            ContaRepository contas,
            TransacaoRepository transacoes,
            ContaService contaService) {

        this.professores = professores;
        this.contas = contas;
        this.transacoes = transacoes;
        this.contaService = contaService;
    }

    @Transactional
    public boolean creditarProfessor(Long professorId) {

        Professor professor = professores.findById(professorId)
            .orElseThrow(() ->
                new NoSuchElementException(
                    "Professor nao encontrado."
                )
            );

        Conta conta = contaService.obterOuCriar(professor);

        Conta bloqueada = contas
            .bloquearPorId(conta.getId())
            .orElseThrow();

        LocalDate hoje = LocalDate.now(ZONA);

        int semestre = hoje.getMonthValue() <= 6 ? 1 : 2;

        String periodo = hoje.getYear() + "-" + semestre;

        // Evita credito duplicado no semestre.
        if (transacoes.existsByConta_IdAndPeriodoSemestre(
                bloqueada.getId(), periodo)) {
            return false;
        }

        int novoSaldo;

        try {
            novoSaldo = Math.addExact(
                bloqueada.getSaldo(), COTA
            );
        } catch (ArithmeticException e) {
            throw new IllegalStateException(
                "Saldo excederia o limite permitido."
            );
        }

        bloqueada.setSaldo(novoSaldo);

        Transacao transacao = new Transacao();
        transacao.setConta(bloqueada);
        transacao.setValor(COTA);
        transacao.setTipo(
            TipoTransacao.CREDITO_SEMESTRAL.name()
        );
        transacao.setMotivo(
            "Credito semestral " + periodo
        );
        transacao.setPeriodoSemestre(periodo);
        transacao.setData(LocalDateTime.now(ZONA));

        transacoes.save(transacao);

        return true;
    }
}
