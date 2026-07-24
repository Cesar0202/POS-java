package com.pos.ventas;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
}
