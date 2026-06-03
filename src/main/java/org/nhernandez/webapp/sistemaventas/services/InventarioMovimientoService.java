package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.MovimientoInventario;
import org.nhernandez.webapp.sistemaventas.models.TipoMovimientoInventario;

import java.util.List;

public interface InventarioMovimientoService {

    void aplicarMovimiento(String tenantOwner, String username, long productoId,
                           TipoMovimientoInventario tipo, int cantidad, String motivo);

    List<MovimientoInventario> listarRecientes(String tenantOwner, int limite);
}
