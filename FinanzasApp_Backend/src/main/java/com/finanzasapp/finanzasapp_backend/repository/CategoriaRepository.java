package com.finanzasapp.finanzasapp_backend.repository;

import com.finanzasapp.finanzasapp_backend.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /** Spring Data genera la consulta a partir del nombre del metodo. */
    List<Categoria> findAllByOrderByTipoAscNombreAsc();
}
