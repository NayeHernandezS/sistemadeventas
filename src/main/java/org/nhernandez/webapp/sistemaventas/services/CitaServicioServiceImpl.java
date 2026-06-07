package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.CitaServicio;
import org.nhernandez.webapp.sistemaventas.models.Cliente;
import org.nhernandez.webapp.sistemaventas.models.EstadoCita;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.repositories.CitaServicioRepository;
import org.nhernandez.webapp.sistemaventas.repositories.ProductoRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CitaServicioServiceImpl implements CitaServicioService {

    private final CitaServicioRepository citaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;
    private final CatalogoPlantillaService catalogoPlantillaService;

    public CitaServicioServiceImpl(CitaServicioRepository citaRepository,
                                   ProductoRepository productoRepository,
                                   ClienteService clienteService,
                                   UsuarioService usuarioService,
                                   CatalogoPlantillaService catalogoPlantillaService) {
        this.citaRepository = citaRepository;
        this.productoRepository = productoRepository;
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
        this.catalogoPlantillaService = catalogoPlantillaService;
    }

    @Override
    public List<CitaServicio> listarDelDia(String tenantOwner, LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        LocalDateTime desde = dia.atStartOfDay();
        LocalDateTime hasta = dia.plusDays(1).atStartOfDay();
        try {
            return citaRepository.listarPorTenantEnRango(tenantOwner, desde, hasta);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public List<Producto> listarServicios(String tenantOwner) {
        try {
            usuarioService.porUsername(tenantOwner).ifPresent(admin -> {
                String tipoNegocio = admin.getTipoNegocio() != null ? admin.getTipoNegocio() : "otro";
                catalogoPlantillaService.asegurarServiciosPlantilla(tenantOwner, tipoNegocio);
            });
            return productoRepository.listarServiciosPorOwner(tenantOwner);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<CitaServicio> porId(String tenantOwner, Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        try {
            return citaRepository.porIdPorTenant(id, tenantOwner);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> validar(CitaServicio cita, boolean esEdicion) {
        Map<String, String> errores = new HashMap<>();
        if (cita.getProductoId() == null || cita.getProductoId() <= 0) {
            errores.put("productoId", "Selecciona un servicio");
        } else {
            validarServicio(cita, errores);
        }
        if (cita.getFechaHora() == null) {
            errores.put("fechaHora", "Indica fecha y hora");
        }
        if (cita.getDuracionMinutos() <= 0) {
            errores.put("duracionMinutos", "La duracion debe ser mayor a 0");
        }
        if (cita.getClienteId() != null && cita.getClienteId() > 0
                && cita.getTenantOwner() != null) {
            Optional<Cliente> cliente = clienteService.porId(cita.getTenantOwner(), cita.getClienteId());
            if (cliente.isEmpty()) {
                errores.put("clienteId", "Cliente no valido");
            }
        }
        if (esEdicion && (cita.getId() == null || cita.getId() <= 0)) {
            errores.put("general", "Cita no identificada");
        }
        return errores;
    }

    @Override
    public CitaServicio guardar(String tenantOwner, String username, CitaServicio cita) {
        Map<String, String> errores = validar(cita, cita.getId() != null && cita.getId() > 0);
        if (!errores.isEmpty()) {
            throw new ServiceJdbcException("Datos de cita incompletos", null);
        }
        cita.setTenantOwner(tenantOwner);
        cita.setUsernameRegistro(username);
        if (cita.getEstado() == null) {
            cita.setEstado(EstadoCita.PROGRAMADA);
        }
        if (cita.getClienteId() != null && cita.getClienteId() <= 0) {
            cita.setClienteId(null);
        }
        try {
            if (cita.getId() != null && cita.getId() > 0) {
                Optional<CitaServicio> existente = citaRepository.porIdPorTenant(cita.getId(), tenantOwner);
                if (existente.isEmpty()) {
                    throw new ServiceJdbcException("Cita no encontrada", null);
                }
                if (!existente.get().isEditable()) {
                    throw new ServiceJdbcException("La cita ya no se puede editar", null);
                }
                citaRepository.actualizar(cita);
            } else {
                cita.setFechaRegistro(LocalDateTime.now());
                citaRepository.guardar(cita);
            }
            return citaRepository.porIdPorTenant(cita.getId(), tenantOwner)
                    .orElse(cita);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void cancelar(String tenantOwner, Long id) {
        actualizarEstadoSeguro(tenantOwner, id, EstadoCita.CANCELADA, null);
    }

    @Override
    public void confirmar(String tenantOwner, Long id) {
        actualizarEstadoSeguro(tenantOwner, id, EstadoCita.CONFIRMADA, null);
    }

    @Override
    public void marcarCompletada(String tenantOwner, Long id) {
        actualizarEstadoSeguro(tenantOwner, id, EstadoCita.COMPLETADA, null);
    }

    private void actualizarEstadoSeguro(String tenantOwner, Long id, EstadoCita estado, Long ticketId) {
        Optional<CitaServicio> cita = porId(tenantOwner, id);
        if (cita.isEmpty()) {
            throw new ServiceJdbcException("Cita no encontrada", null);
        }
        if (cita.get().getEstado() == EstadoCita.CANCELADA) {
            throw new ServiceJdbcException("La cita ya esta cancelada", null);
        }
        if (cita.get().getEstado() == EstadoCita.COMPLETADA && estado != EstadoCita.COMPLETADA) {
            throw new ServiceJdbcException("La cita ya esta completada", null);
        }
        try {
            citaRepository.actualizarEstado(id, tenantOwner, estado, ticketId);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    private void validarServicio(CitaServicio cita, Map<String, String> errores) {
        try {
            Producto producto = productoRepository.porIdPorOwner(cita.getProductoId(), cita.getTenantOwner());
            if (producto == null) {
                errores.put("productoId", "Servicio no encontrado");
            } else if (!producto.esServicio()) {
                errores.put("productoId", "Solo puedes agendar servicios del catalogo");
            }
        } catch (SQLException e) {
            errores.put("productoId", "No se pudo validar el servicio");
        }
    }
}
