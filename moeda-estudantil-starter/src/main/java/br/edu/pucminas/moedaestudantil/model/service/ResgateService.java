
package br.edu.pucminas.moedaestudantil.model.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

import br.edu.pucminas.moedaestudantil.dto.request.ResgateRequest;
import br.edu.pucminas.moedaestudantil.dto.response.ResgateResponse;
import br.edu.pucminas.moedaestudantil.model.entity.*;
import br.edu.pucminas.moedaestudantil.model.enums.TipoTransacao;
import br.edu.pucminas.moedaestudantil.model.repository.*;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResgateService {

    private static final ZoneId ZONA =
        ZoneId.of("America/Sao_Paulo");

    private final UsuarioRepository usuarios;
    private final VantagemRepository vantagens;
    private final ResgateRepository resgates;
    private final ContaRepository contas;
    private final TransacaoRepository transacoes;
    private final ContaService contaService;
    private final NotificacaoService notificacoes;

    public ResgateService(
            UsuarioRepository usuarios,
            VantagemRepository vantagens,
            ResgateRepository resgates,
            ContaRepository contas,
            TransacaoRepository transacoes,
            ContaService contaService,
            NotificacaoService notificacoes) {

        this.usuarios = usuarios;
        this.vantagens = vantagens;
        this.resgates = resgates;
        this.contas = contas;
        this.transacoes = transacoes;
        this.contaService = contaService;
        this.notificacoes = notificacoes;
    }

    @Transactional
    public ResgateResponse resgatar(
            String login,
            ResgateRequest dados) {

        Aluno aluno = localizarAluno(login);

        Vantagem vantagem = vantagens
            .findById(dados.vantagemId())
            .orElseThrow(() ->
                new NoSuchElementException(
                    "Vantagem nao encontrada."
                )
            );

        int custo = vantagem.getCustoMoedas();

        if (custo <= 0) {
            throw new IllegalStateException(
                "Esta vantagem possui custo invalido."
            );
        }

        // Localiza ou cria a carteira do aluno
        Conta conta = contaService.obterOuCriar(aluno);

        // Bloqueia a conta durante o resgate
        Conta bloqueada = contas
            .bloquearPorId(conta.getId())
            .orElseThrow(() ->
                new NoSuchElementException(
                    "Conta nao encontrada."
                )
            );

        if (bloqueada.getSaldo() < custo) {
            throw new IllegalArgumentException(
                "Saldo insuficiente para resgatar esta vantagem."
            );
        }

        // Desconta as moedas
        bloqueada.setSaldo(
            bloqueada.getSaldo() - custo
        );

        LocalDateTime agora = LocalDateTime.now(ZONA);

        // Gera um cupom aleatorio e unico
        String codigoCupom = "MDE-" +
            UUID.randomUUID().toString()
                .replace("-", "")
                .toUpperCase(Locale.ROOT);

        // Registra o resgate
        Resgate resgate = new Resgate();

        resgate.setAluno(aluno);
        resgate.setVantagem(vantagem);
        resgate.setCodigoCupom(codigoCupom);
        resgate.setData(agora);

        resgate = resgates.saveAndFlush(resgate);

        // Registra a saida no extrato
        Transacao transacao = new Transacao();

        transacao.setConta(bloqueada);
        transacao.setValor(-custo);
        transacao.setTipo(TipoTransacao.RESGATE.name());
        transacao.setMotivo(
            "Resgate: " + vantagem.getDescricao()
        );
        transacao.setData(agora);
        transacao.setOperacaoId(
            UUID.randomUUID().toString()
        );

        transacoes.save(transacao);

        // Notificacoes do aluno e da empresa
        notificacoes.registrarResgate(
            aluno, vantagem, resgate, custo
        );

        return converter(resgate);
    }

    @Transactional(readOnly = true)
    public List<ResgateResponse> meusResgates(String login) {

        Aluno aluno = localizarAluno(login);

        return resgates
            .findByAluno_IdOrderByDataDescIdDesc(
                aluno.getId()
            )
            .stream()
            .map(this::converter)
            .toList();
    }

    private Aluno localizarAluno(String login) {

        Usuario usuario = usuarios
            .findByLoginIgnoreCase(login)
            .orElseThrow(() ->
                new NoSuchElementException(
                    "Usuario nao encontrado."
                )
            );

        if (!(usuario instanceof Aluno aluno)) {
            throw new AccessDeniedException(
                "Somente alunos podem resgatar vantagens."
            );
        }

        return aluno;
    }

    private ResgateResponse converter(Resgate resgate) {

        Vantagem vantagem = resgate.getVantagem();

        return new ResgateResponse(
            resgate.getId(),
            vantagem.getId(),
            vantagem.getDescricao(),
            vantagem.getEmpresa().getNome(),
            vantagem.getCustoMoedas(),
            resgate.getCodigoCupom(),
            resgate.getData()
        );
    }
}
