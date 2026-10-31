
package br.edu.pucminas.moedaestudantil.infrastructure.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailGatewayPadrao implements EmailGateway {

    private static final Logger log =
        LoggerFactory.getLogger(EmailGatewayPadrao.class);

    private final ObjectProvider<JavaMailSender> senderProvider;
    private final boolean habilitado;
    private final String remetente;

    public EmailGatewayPadrao(
            ObjectProvider<JavaMailSender> senderProvider,
            @Value("${app.email.enabled:false}")
            boolean habilitado,
            @Value("${app.email.from:}")
            String remetente) {

        this.senderProvider = senderProvider;
        this.habilitado = habilitado;
        this.remetente = remetente;
    }

    @Override
    public void enviar(
            String destinatario,
            String assunto,
            String mensagem) {

        // Modo de desenvolvimento: nao envia email real.
        if (!habilitado) {
            log.info(
                "[EMAIL SIMULADO] Para: {} | Assunto: {} | Mensagem: {}",
                destinatario,
                assunto,
                mensagem.replace("\n", " | ")
            );
            return;
        }

        JavaMailSender sender = senderProvider.getIfAvailable();

        if (sender == null || remetente.isBlank()) {
            throw new IllegalStateException(
                "SMTP nao configurado corretamente."
            );
        }

        SimpleMailMessage email = new SimpleMailMessage();

        email.setFrom(remetente);
        email.setTo(destinatario);
        email.setSubject(assunto);
        email.setText(mensagem);

        sender.send(email);
    }
}
