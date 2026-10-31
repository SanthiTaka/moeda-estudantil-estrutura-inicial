
package br.edu.pucminas.moedaestudantil.model.service;

import br.edu.pucminas.moedaestudantil.infrastructure.email.*;
import br.edu.pucminas.moedaestudantil.model.entity.*;
import br.edu.pucminas.moedaestudantil.model.repository.NotificacaoRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacoes;
    private final ApplicationEventPublisher eventos;

    public NotificacaoService(
            NotificacaoRepository notificacoes,
            ApplicationEventPublisher eventos) {

        this.notificacoes = notificacoes;
        this.eventos = eventos;
    }

    // Mantem o recebimento de moedas que ja funciona
    public void registrarRecebimento(
            Aluno aluno,
            int quantidade,
            String motivo,
            String nomeProfessor) {

        registrar(
            aluno,
            "RECEBIMENTO_MOEDAS",
            "Voce recebeu " + quantidade +
            " moedas em sua conta."
        );

        eventos.publishEvent(
            new MoedasRecebidasEvent(
                aluno.getEmail(),
                aluno.getNome(),
                nomeProfessor,
                quantidade,
                motivo
            )
        );
    }

    // Nova notificacao de resgate
    public void registrarResgate(
            Aluno aluno,
            Vantagem vantagem,
            Resgate resgate,
            int custo) {

        EmpresaParceira empresa = vantagem.getEmpresa();

        registrar(
            aluno,
            "CUPOM_RESGATE",
            "Resgate realizado: " +
            vantagem.getDescricao() +
            ". Cupom: " + resgate.getCodigoCupom()
        );

        registrar(
            empresa,
            "CONFERENCIA_EMPRESA",
            "Novo resgate de " +
            vantagem.getDescricao() +
            ". Cupom: " + resgate.getCodigoCupom()
        );

        eventos.publishEvent(
            new CupomResgatadoEvent(
                aluno.getEmail(),
                aluno.getNome(),
                empresa.getEmail(),
                empresa.getNome(),
                vantagem.getDescricao(),
                resgate.getCodigoCupom(),
                custo
            )
        );
    }

    private void registrar(
            Usuario usuario,
            String tipo,
            String mensagem) {

        Notificacao notificacao = new Notificacao();

        notificacao.setUsuario(usuario);
        notificacao.setTipo(tipo);
        notificacao.setMensagem(mensagem);

        notificacoes.save(notificacao);
    }
}
