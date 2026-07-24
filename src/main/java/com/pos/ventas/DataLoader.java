package com.pos.ventas;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.pos.mesas.Mesa;
import com.pos.mesas.EstadoMesa;
import com.pos.mesas.MesaRepository;
import com.pos.seguridad.Rol;
import com.pos.seguridad.Usuario;
import com.pos.seguridad.UsuarioRepository;

import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(ProductoRepository repository, CategoriaRepository categoriaRepository, MesaRepository mesaRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (categoriaRepository.count() == 0) {
                Categoria catPlatos = categoriaRepository.save(new Categoria("Platos", "bg-warning"));
                Categoria catBebidas = categoriaRepository.save(new Categoria("Bebidas", "bg-info"));
                Categoria catSnacks = categoriaRepository.save(new Categoria("Snacks", "bg-success"));

                if (repository.count() == 0) {
                    repository.saveAll(List.of(
                        new Producto("Hamburguesa Clásica", 8.50, 50, catPlatos),
                        new Producto("Hamburguesa Doble", 11.00, 50, catPlatos),
                        new Producto("Papas Fritas", 3.50, 100, catSnacks),
                        new Producto("Refresco Cola", 2.00, 100, catBebidas),
                        new Producto("Cerveza Artesanal", 4.50, 80, catBebidas),
                        new Producto("Nachos con Queso", 6.00, 50, catSnacks),
                        new Producto("Agua Mineral", 1.50, 100, catBebidas)
                    ));
                }
            }
            
            if (mesaRepository.count() == 0) {
                for (int i = 1; i <= 20; i++) {
                    Mesa mesa = new Mesa();
                    mesa.setNumero(i);
                    mesa.setEstado(EstadoMesa.LIBRE);
                    mesaRepository.save(mesa);
                }
            }

            if (usuarioRepository.count() == 0) {
                usuarioRepository.saveAll(List.of(
                    new Usuario("admin", passwordEncoder.encode("admin123"), Rol.ADMIN),
                    new Usuario("cajero", passwordEncoder.encode("caja123"), Rol.CAJERO)
                ));
            }
        };
    }
}
