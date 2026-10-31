
package br.edu.pucminas.moedaestudantil.infrastructure.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CupomResgatadoEmailListener {

    private static final Logger log =
        LoggerFactory.getLogger(CupomResgatadoEmailListener.class);

    private final EmailGateway emailGateway;

    public CupomResgatadoEmailListener(
            EmailGateway emailGateway) {
        this.emailGateway = emailGateway;
    }

    @TransactionalEventListener(
        phase = TransactionPhase.AFTER_COMMIT
    )
    public void notificar(CupomResgatadoEvent evento) {

        String mensagemAluno = """
            Ola, %s!

            Seu resgate foi realizado com sucesso.

            Vantagem: %s
            Empresa: %s
            Custo: %d moedas

            SEU CUPOM:
            %s

            Apresente o codigo a empresa parceira
            para utilizar sua vantagem.

            Sistema de Moeda Estudantil
            """.formatted(
                evento.nomeAluno(),
                evento.descricaoVantagem(),
                evento.nomeEmpresa(),
                evento.custoMoedas(),
                evento.codigoCupom()
            );

        try {
            emailGateway.enviar(
                evento.emailAluno(),
                "Moeda Estudantil - Seu cupom de resgate",
                mensagemAluno
            );
        } catch (RuntimeException erro) {
            log.warn(
                "Falha no email do aluno: {}",
                erro.getMessage()
            );
        }

        String mensagemEmpresa = """
            Ola, %s!

            Uma vantagem da sua empresa foi resgatada.

            Vantagem: %s
            Aluno: %s

            CODIGO DE CONFERENCIA:
            %s

            Utilize este codigo para conferir
            a solicitacao do aluno.

            Sistema de Moeda Estudantil
            """.formatted(
                evento.nomeEmpresa(),
                evento.descricaoVantagem(),
                evento.nomeAluno(),
                evento.codigoCupom()
            );

        try {
            emailGateway.enviar(
                evento.emailEmpresa(),
                "Moeda Estudantil - Novo resgate",
                mensagemEmpresa
            );
        } catch (RuntimeException erro) {
            log.warn(
                "Falha no email da empresa: {}",
                erro.getMessage()
            );
        }
    }
}
