package br.edu.pucminas.moedaestudantil.model.repository;

import br.edu.pucminas.moedaestudantil.model.entity.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
}
