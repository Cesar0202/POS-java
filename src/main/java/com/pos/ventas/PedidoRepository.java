package com.pos.ventas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByMesaIdAndEstado(Long mesaId, EstadoPedido estado);
}
