
package br.edu.pucminas.moedaestudantil.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.pucminas.moedaestudantil.model.entity.Transacao;

public interface TransacaoRepository
        extends JpaRepository<Transacao, Long> {

    List<Transacao> findByConta_IdOrderByDataDescIdDesc(
        Long contaId
    );

    boolean existsByConta_IdAndPeriodoSemestre(
        Long contaId,
        String periodoSemestre
    );
}
