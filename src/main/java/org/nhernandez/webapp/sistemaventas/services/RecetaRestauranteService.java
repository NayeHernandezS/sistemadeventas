package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.PlatilloCostoResumen;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.RecetaLinea;

import java.util.List;
import java.util.Optional;

public interface RecetaRestauranteService {

    List<PlatilloCostoResumen> listarResumenPlatillos(String tenantOwner);

    List<Producto> listarInsumos(String tenantOwner);

    List<Producto> listarPlatillos(String tenantOwner);

    List<RecetaLinea> lineasConCosto(String tenantOwner, Long productoId);

    Optional<Producto> platilloPorId(String tenantOwner, Long productoId);

    void guardarReceta(String tenantOwner, Long productoId, List<RecetaLinea> lineas);

    void eliminarReceta(String tenantOwner, Long productoId);

    boolean aplicaParaTenant(String tipoNegocio);
}
