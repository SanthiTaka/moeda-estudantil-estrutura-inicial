package br.edu.pucminas.moedaestudantil.infrastructure.email;

/** Contrato de envio. A implementacao SMTP sera criada na etapa de notificacoes. */
public interface EmailGateway {
    void enviar(String destinatario, String assunto, String mensagem);
}
