package com.pos.ventas;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public ReporteController(PedidoRepository pedidoRepository, ItemPedidoRepository itemPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    @GetMapping("/hoy")
    public Map<String, Object> reporteDiario() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1).minusNanos(1);

        List<Pedido> pedidosHoy = pedidoRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoPedido.PAGADO)
                .filter(p -> p.getFechaApertura().isAfter(inicioDia) && p.getFechaApertura().isBefore(finDia))
                .collect(Collectors.toList());

        double totalVentas = 0;
        double totalPropinas = 0;
        double ventasEfectivo = 0;
        double ventasTarjeta = 0;
        double ventasTransferencia = 0;

        for (Pedido p : pedidosHoy) {
            double pagado = p.getTotal();
            totalVentas += pagado;
            
            if (p.getPropina() != null) {
                totalPropinas += p.getPropina();
            }

            if (p.getMetodoPago() == MetodoPago.EFECTIVO) ventasEfectivo += pagado;
            else if (p.getMetodoPago() == MetodoPago.TARJETA) ventasTarjeta += pagado;
            else if (p.getMetodoPago() == MetodoPago.TRANSFERENCIA) ventasTransferencia += pagado;
        }

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("pedidosCobrados", pedidosHoy.size());
        reporte.put("totalVentas", totalVentas);
        reporte.put("totalPropinas", totalPropinas);
        reporte.put("ventasEfectivo", ventasEfectivo);
        reporte.put("ventasTarjeta", ventasTarjeta);
        reporte.put("ventasTransferencia", ventasTransferencia);

        return reporte;
    }

    @GetMapping("/hoy-detalle")
    public List<Map<String, Object>> reporteDiarioDetalle() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1).minusNanos(1);

        List<ItemPedido> itemsHoy = itemPedidoRepository.findAll().stream()
                .filter(i -> i.getPedido() != null && i.getPedido().getEstado() == EstadoPedido.PAGADO)
                .filter(i -> i.getPedido().getFechaApertura().isAfter(inicioDia) && i.getPedido().getFechaApertura().isBefore(finDia))
                .collect(Collectors.toList());

        Map<String, double[]> agrupado = new HashMap<>(); // [0] = cantidad, [1] = subtotal
        for (ItemPedido i : itemsHoy) {
            String prod = i.getProducto().getNombre();
            double[] vals = agrupado.getOrDefault(prod, new double[]{0, 0});
            vals[0] += i.getCantidad();
            vals[1] += i.getSubtotal();
            agrupado.put(prod, vals);
        }

        return agrupado.entrySet().stream()
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("producto", e.getKey());
                    map.put("cantidad", e.getValue()[0]);
                    map.put("total", e.getValue()[1]);
                    return map;
                })
                .sorted((a, b) -> Double.compare((double)b.get("total"), (double)a.get("total")))
                .collect(Collectors.toList());
    }

    @GetMapping("/ventas-7-dias")
    public List<Map<String, Object>> ventasUltimos7Dias() {
        LocalDateTime inicio = LocalDate.now().minusDays(6).atStartOfDay();
        
        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoPedido.PAGADO && p.getFechaApertura().isAfter(inicio))
                .collect(Collectors.toList());

        Map<LocalDate, Double> agrupado = pedidos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getFechaApertura().toLocalDate(),
                        Collectors.summingDouble(Pedido::getTotal)
                ));

        return agrupado.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("fecha", e.getKey().toString());
                    map.put("total", e.getValue());
                    return map;
                }).collect(Collectors.toList());
    }

    @GetMapping("/top-productos")
    public List<Map<String, Object>> topProductos() {
        List<ItemPedido> items = itemPedidoRepository.findAll().stream()
                .filter(i -> i.getPedido() != null && i.getPedido().getEstado() == EstadoPedido.PAGADO)
                .collect(Collectors.toList());

        Map<String, Integer> agrupado = items.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getProducto().getNombre(),
                        Collectors.summingInt(ItemPedido::getCantidad)
                ));

        return agrupado.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("producto", e.getKey());
                    map.put("cantidad", e.getValue());
                    return map;
                }).collect(Collectors.toList());
    }
}
