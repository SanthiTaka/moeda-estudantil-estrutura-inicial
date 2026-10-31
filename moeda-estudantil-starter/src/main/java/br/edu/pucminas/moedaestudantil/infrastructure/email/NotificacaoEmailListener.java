
package br.edu.pucminas.moedaestudantil.infrastructure.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificacaoEmailListener {

    private static final Logger log =
        LoggerFactory.getLogger(NotificacaoEmailListener.class);

    private final EmailGateway emailGateway;

    public NotificacaoEmailListener(EmailGateway emailGateway) {
        this.emailGateway = emailGateway;
    }

    @TransactionalEventListener(
        phase = TransactionPhase.AFTER_COMMIT
    )
    public void enviarEmail(MoedasRecebidasEvent evento) {

        String assunto =
            "Moeda Estudantil - Voce recebeu moedas!";

        String mensagem = """
            Ola, %s!

            Voce recebeu %d moedas estudantis!

            Professor: %s
            Motivo: %s

            Acesse o Sistema de Moeda Estudantil
            para consultar seu saldo e extrato.

            Parabens pelo reconhecimento!
            """.formatted(
                evento.nomeAluno(),
                evento.quantidade(),
                evento.nomeProfessor(),
                evento.motivo()
            );

        try {
            emailGateway.enviar(
                evento.emailAluno(),
                assunto,
                mensagem
            );
        } catch (RuntimeException erro) {
            // A transferencia ja foi confirmada no banco.
            // Uma falha no email nao pode desfazer as moedas.
            log.warn(
                "Falha ao enviar notificacao por email: {}",
                erro.getMessage()
            );
        }
    }
}
