
package br.edu.pucminas.moedaestudantil.model.repository;

import br.edu.pucminas.moedaestudantil.model.entity.Resgate;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResgateRepository
        extends JpaRepository<Resgate, Long> {

    boolean existsByVantagem_Id(Long vantagemId);
}
