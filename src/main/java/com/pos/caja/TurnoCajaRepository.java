package com.pos.caja;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TurnoCajaRepository extends JpaRepository<TurnoCaja, Long> {
    Optional<TurnoCaja> findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja estado);
}
