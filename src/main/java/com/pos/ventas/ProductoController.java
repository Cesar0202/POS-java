package com.pos.ventas;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoController(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Producto producto = productoRepository.findById(id).orElse(null);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }

        if (updates.containsKey("precio")) {
            producto.setPrecio(((Number) updates.get("precio")).doubleValue());
        }
        if (updates.containsKey("stock")) {
            producto.setStock(((Number) updates.get("stock")).intValue());
        }
        if (updates.containsKey("nombre")) {
            producto.setNombre((String) updates.get("nombre"));
        }
        if (updates.containsKey("categoriaId")) {
            Object catIdObj = updates.get("categoriaId");
            if (catIdObj == null) {
                producto.setCategoria(null);
            } else {
                Long categoriaId = ((Number) catIdObj).longValue();
                Categoria categoria = categoriaRepository.findById(categoriaId).orElse(null);
                if (categoria != null) {
                    producto.setCategoria(categoria);
                }
            }
        }

        return ResponseEntity.ok(productoRepository.save(producto));
    }

    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody Map<String, Object> payload) {
        if (!payload.containsKey("nombre") || !payload.containsKey("precio") || !payload.containsKey("stock") || !payload.containsKey("categoriaId")) {
            return ResponseEntity.badRequest().build();
        }

        String nombre = (String) payload.get("nombre");
        Double precio = ((Number) payload.get("precio")).doubleValue();
        Integer stock = ((Number) payload.get("stock")).intValue();
        Long categoriaId = ((Number) payload.get("categoriaId")).longValue();

        Categoria categoria = categoriaRepository.findById(categoriaId).orElse(null);
        if (categoria == null) {
            return ResponseEntity.badRequest().build();
        }

        Producto nuevoProducto = new Producto(nombre, precio, stock, categoria);
        return ResponseEntity.ok(productoRepository.save(nuevoProducto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        if (!productoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
