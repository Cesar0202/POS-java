package com.pos.mesas;

import com.pos.ventas.PedidoService;
import com.pos.ventas.CobroRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MesaService {

    private final MesaRepository mesaRepository;
    private final com.pos.ventas.PedidoService pedidoService;

    public MesaService(MesaRepository mesaRepository, com.pos.ventas.PedidoService pedidoService) {
        this.mesaRepository = mesaRepository;
        this.pedidoService = pedidoService;
    }

    public List<Mesa> listarTodas() {
        return mesaRepository.findAll();
    }

    @Transactional
    public Mesa agregarMesa() {
        Integer maxNumero = mesaRepository.findMaxNumero().orElse(0);
        Mesa nuevaMesa = new Mesa(maxNumero + 1, EstadoMesa.LIBRE);
        return mesaRepository.save(nuevaMesa);
    }

    @Transactional
    public void eliminarMesa(Long id) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada con id: " + id));
        
        if (mesa.getEstado() != EstadoMesa.LIBRE) {
            throw new IllegalStateException("No se puede eliminar una mesa que no está LIBRE.");
        }
        
        mesaRepository.delete(mesa);
    }

    @Transactional
    public Mesa ocuparMesa(Long id) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada con id: " + id));
        
        if (mesa.getEstado() != EstadoMesa.LIBRE) {
            throw new IllegalStateException("La mesa ya está ocupada o reservada.");
        }
        
        mesa.setEstado(EstadoMesa.OCUPADA);
        
        com.pos.ventas.Pedido nuevoPedido = pedidoService.crearPedidoParaMesa(mesa.getId());
        mesa.setVentaActivaId(nuevoPedido.getId());
        
        return mesaRepository.save(mesa);
    }

    @Transactional
    public Mesa liberarMesa(Long id, CobroRequest request) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada con ID: " + id));
        
        if (mesa.getVentaActivaId() != null) {
            pedidoService.cobrarPedido(mesa.getVentaActivaId(), request);
            mesa.setVentaActivaId(null);
        }
        
        mesa.setEstado(EstadoMesa.LIBRE);
        return mesaRepository.save(mesa);
    }
}
