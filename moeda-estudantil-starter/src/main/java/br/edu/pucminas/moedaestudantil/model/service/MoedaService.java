
package br.edu.pucminas.moedaestudantil.model.service;

import java.time.LocalDateTime;
import java.util.*;

import br.edu.pucminas.moedaestudantil.dto.request.EnviarMoedasRequest;
import br.edu.pucminas.moedaestudantil.dto.response.AlunoDestinoResponse;
import br.edu.pucminas.moedaestudantil.model.entity.*;
import br.edu.pucminas.moedaestudantil.model.enums.TipoTransacao;
import br.edu.pucminas.moedaestudantil.model.repository.*;

import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MoedaService {

    private final UsuarioRepository usuarios;
    private final AlunoRepository alunos;
    private final ContaRepository contas;
    private final TransacaoRepository transacoes;
    private final ContaService contaService;
    private final NotificacaoService notificacaoService;

    public MoedaService(
            UsuarioRepository usuarios,
            AlunoRepository alunos,
            ContaRepository contas,
            TransacaoRepository transacoes,
            ContaService contaService,
            NotificacaoService notificacaoService) {

        this.usuarios = usuarios;
        this.alunos = alunos;
        this.contas = contas;
        this.transacoes = transacoes;
        this.contaService = contaService;
        this.notificacaoService = notificacaoService;
    }

    @Transactional(readOnly = true)
    public List<AlunoDestinoResponse> listarAlunos() {
        return alunos.findAll(Sort.by("nome")).stream()
            .map(a -> new AlunoDestinoResponse(
                a.getId(), a.getNome()
            ))
            .toList();
    }

    @Transactional
    public void enviar(
            String loginProfessor,
            EnviarMoedasRequest dados) {

        Usuario usuario = usuarios
            .findByLoginIgnoreCase(loginProfessor)
            .orElseThrow(() ->
                new NoSuchElementException(
                    "Usuario nao encontrado."
                )
            );

        if (!(usuario instanceof Professor)) {
            throw new AccessDeniedException(
                "Somente professores podem enviar moedas."
            );
        }

        Aluno aluno = alunos.findById(dados.alunoId())
            .orElseThrow(() ->
                new NoSuchElementException(
                    "Aluno nao encontrado."
                )
            );

        int quantidade = dados.quantidade();
        String motivo = dados.motivo().trim();

        if (quantidade <= 0 || motivo.isBlank()) {
            throw new IllegalArgumentException(
                "Informe quantidade e motivo validos."
            );
        }

        Conta origem = contaService.obterOuCriar(usuario);
        Conta destino = contaService.obterOuCriar(aluno);

        // Bloqueia sempre na mesma ordem para
        // reduzir o risco de deadlock.
        List<Long> ids = new ArrayList<>(List.of(
            origem.getId(), destino.getId()
        ));
        Collections.sort(ids);

        Map<Long, Conta> bloqueadas = new HashMap<>();

        for (Long id : ids) {
            Conta conta = contas.bloquearPorId(id)
                .orElseThrow();
            bloqueadas.put(id, conta);
        }

        Conta contaProfessor =
            bloqueadas.get(origem.getId());

        Conta contaAluno =
            bloqueadas.get(destino.getId());

        if (contaProfessor.getSaldo() < quantidade) {
            throw new IllegalArgumentException(
                "Saldo insuficiente para enviar moedas."
            );
        }

        int novoSaldoAluno;

        try {
            novoSaldoAluno = Math.addExact(
                contaAluno.getSaldo(), quantidade
            );
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                "Saldo do aluno excederia o limite permitido."
            );
        }

        contaProfessor.setSaldo(
            contaProfessor.getSaldo() - quantidade
        );

        contaAluno.setSaldo(novoSaldoAluno);

        String operacaoId = UUID.randomUUID().toString();
        LocalDateTime agora = LocalDateTime.now();

        registrar(
            contaProfessor, -quantidade,
            TipoTransacao.ENVIO.name(),
            motivo, operacaoId, agora
        );

        registrar(
            contaAluno, quantidade,
            TipoTransacao.RECEBIMENTO.name(),
            motivo, operacaoId, agora
        );

        notificacaoService.registrarRecebimento(
    aluno,
    quantidade,
    motivo,
    usuario.getNome()
);
    }

    private void registrar(
            Conta conta,
            int valor,
            String tipo,
            String motivo,
            String operacaoId,
            LocalDateTime data) {

        Transacao transacao = new Transacao();

        transacao.setConta(conta);
        transacao.setValor(valor);
        transacao.setTipo(tipo);
        transacao.setMotivo(motivo);
        transacao.setOperacaoId(operacaoId);
        transacao.setData(data);

        transacoes.save(transacao);
    }
}
