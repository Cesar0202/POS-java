package com.pos.mesas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {
    
    @Query("SELECT MAX(m.numero) FROM Mesa m")
    Optional<Integer> findMaxNumero();
}
