
package br.edu.pucminas.moedaestudantil.model.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.pucminas.moedaestudantil.model.entity.Conta;

public interface ContaRepository
        extends JpaRepository<Conta, Long> {

    Optional<Conta> findByUsuario_Id(Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conta c where c.id = :id")
    Optional<Conta> bloquearPorId(@Param("id") Long id);
}
