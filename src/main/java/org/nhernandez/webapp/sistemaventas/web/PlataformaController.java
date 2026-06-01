package org.nhernandez.webapp.sistemaventas.web;

import org.nhernandez.webapp.sistemaventas.models.ClienteCuenta;
import org.nhernandez.webapp.sistemaventas.models.PlanSuscripcion;
import org.nhernandez.webapp.sistemaventas.services.PagoSuscripcionExpiracionService;
import org.nhernandez.webapp.sistemaventas.services.PlataformaService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.services.SoporteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/plataforma")
public class PlataformaController {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PlataformaService plataformaService;
    private final SoporteService soporteService;
    private final PagoSuscripcionExpiracionService expiracionService;

    public PlataformaController(PlataformaService plataformaService,
                                SoporteService soporteService,
                                PagoSuscripcionExpiracionService expiracionService) {
        this.plataformaService = plataformaService;
        this.soporteService = soporteService;
        this.expiracionService = expiracionService;
    }

    @GetMapping
    public String inicio(Model model) {
        model.addAttribute("totalClientes", plataformaService.listarClientes().size());
        model.addAttribute("pagosPendientes", plataformaService.pagosPendientesGlobales().size());
        model.addAttribute("pagosExpirados", plataformaService.pagosExpiradosGlobales().size());
        model.addAttribute("soporteAbiertas", soporteService.listarAbiertasPlataforma().size());
        return "plataforma/inicio";
    }

    @GetMapping("/soporte")
    public String soportePlataforma(Model model) {
        model.addAttribute("solicitudes", soporteService.listarAbiertasPlataforma());
        return "plataforma/soporte";
    }

    @PostMapping("/soporte/atender")
    public String atenderSoporte(@RequestParam("id") long id, Model model) {
        soporteService.marcarAtendida(id);
        model.addAttribute("mensajeExito", "Solicitud marcada como atendida.");
        model.addAttribute("solicitudes", soporteService.listarAbiertasPlataforma());
        return "plataforma/soporte";
    }

    @GetMapping("/clientes")
    public String clientes(Model model) {
        cargarVistaClientes(model);
        return "plataforma/clientes";
    }

    @GetMapping("/clientes/detalle")
    public String detalleCliente(@RequestParam("username") String username, Model model) {
        return plataformaService.buscarCliente(username)
                .map(cliente -> {
                    cargarVistaDetalle(model, cliente);
                    return "plataforma/clienteDetalle";
                })
                .orElseGet(() -> {
                    model.addAttribute("mensajeError", "Cliente no encontrado.");
                    cargarVistaClientes(model);
                    return "plataforma/clientes";
                });
    }

    @PostMapping("/clientes")
    public String clientesAccion(@RequestParam("accion") String accion,
                                 @RequestParam("username") String username,
                                 @RequestParam(value = "meses", required = false) Integer meses,
                                 @RequestParam(value = "planCodigo", required = false) String planCodigo,
                                 @RequestParam(value = "desdeDetalle", defaultValue = "false") boolean desdeDetalle,
                                 Model model) {
        String cuenta = username != null ? username.trim() : "";
        try {
            switch (accion != null ? accion.trim() : "") {
                case "extender" -> {
                    int m = meses != null ? meses : 0;
                    if (m < 1 || m > 24) {
                        throw new ServiceJdbcException("Elige entre 1 y 24 meses", null);
                    }
                    plataformaService.extenderMeses(cuenta, m);
                    model.addAttribute("mensajeExito",
                            "Suscripcion de " + cuenta + " extendida " + m + " mes(es).");
                }
                case "suspender" -> {
                    plataformaService.suspenderCuenta(cuenta);
                    model.addAttribute("mensajeExito", "Cuenta " + cuenta + " suspendida.");
                }
                case "reactivar" -> {
                    plataformaService.reactivarCuenta(cuenta);
                    model.addAttribute("mensajeExito", "Cuenta " + cuenta + " reactivada.");
                }
                case "cambiarPlan" -> {
                    if (planCodigo == null || planCodigo.isBlank()) {
                        throw new ServiceJdbcException("Selecciona un plan", null);
                    }
                    plataformaService.cambiarPlan(cuenta, planCodigo.trim().toUpperCase());
                    model.addAttribute("mensajeExito",
                            "Plan de " + cuenta + " actualizado a " + planCodigo.trim().toUpperCase() + ".");
                }
                default -> model.addAttribute("mensajeError", "Accion no valida.");
            }
        } catch (ServiceJdbcException e) {
            model.addAttribute("mensajeError", e.getMessage());
        }
        if (desdeDetalle && !cuenta.isBlank()) {
            return plataformaService.buscarCliente(cuenta)
                    .map(c -> {
                        cargarVistaDetalle(model, c);
                        return "plataforma/clienteDetalle";
                    })
                    .orElseGet(() -> {
                        cargarVistaClientes(model);
                        return "plataforma/clientes";
                    });
        }
        cargarVistaClientes(model);
        return "plataforma/clientes";
    }

    @PostMapping("/clientes/extender")
    public String extenderDesdeLista(@RequestParam("username") String username,
                                     @RequestParam("meses") int meses,
                                     Model model) {
        return clientesAccion("extender", username, meses, null, false, model);
    }

    private void cargarVistaClientes(Model model) {
        model.addAttribute("clientes", plataformaService.listarClientes());
        model.addAttribute("formatoFecha", FORMATO);
    }

    private void cargarVistaDetalle(Model model, ClienteCuenta cliente) {
        model.addAttribute("cliente", cliente);
        model.addAttribute("pagos", plataformaService.pagosDelCliente(cliente.getUsername()));
        model.addAttribute("planes", plataformaService.planesDisponibles());
        model.addAttribute("formatoFecha", FORMATO);
        suscripcionEtiquetas(cliente, model);
    }

    private static void suscripcionEtiquetas(ClienteCuenta cliente, Model model) {
        boolean suspendida = cliente.getEstadoSuscripcion() != null
                && "SUSPENDIDA".equalsIgnoreCase(cliente.getEstadoSuscripcion().trim());
        model.addAttribute("cuentaSuspendida", suspendida);
        PlanSuscripcion.porCodigo(cliente.getPlanCodigo())
                .ifPresent(p -> model.addAttribute("planNombre", p.getNombre()));
        if (!model.containsAttribute("planNombre")) {
            model.addAttribute("planNombre", cliente.getPlanCodigo());
        }
    }

    @GetMapping("/pagos")
    public String pagos(Model model) {
        cargarVistaPagos(model);
        return "plataforma/pagos";
    }

    @PostMapping("/pagos")
    public String pagosAccion(@RequestParam("accion") String accion,
                              @RequestParam(value = "pagoId", required = false) Long pagoId,
                              Model model) {
        try {
            switch (accion != null ? accion.trim() : "") {
                case "confirmar" -> {
                    if (pagoId == null) {
                        throw new ServiceJdbcException("Pago no valido", null);
                    }
                    plataformaService.confirmarPago(pagoId);
                    model.addAttribute("mensajeExito", "Pago confirmado y suscripcion extendida.");
                }
                case "expirar" -> {
                    if (pagoId == null) {
                        throw new ServiceJdbcException("Pago no valido", null);
                    }
                    plataformaService.expirarPago(pagoId);
                    model.addAttribute("mensajeExito", "Pago marcado como expirado.");
                }
                case "expirarVencidos" -> {
                    int expirados = plataformaService.expirarPagosVencidos();
                    if (expirados > 0) {
                        model.addAttribute("mensajeExito",
                                expirados + " pago(s) vencido(s) marcado(s) como expirado(s).");
                    } else {
                        model.addAttribute("mensajeExito", "No habia pagos pendientes fuera de plazo.");
                    }
                }
                default -> model.addAttribute("mensajeError", "Accion no valida.");
            }
        } catch (ServiceJdbcException e) {
            model.addAttribute("mensajeError", e.getMessage());
        }
        cargarVistaPagos(model);
        return "plataforma/pagos";
    }

    @PostMapping("/pagos/confirmar")
    public String confirmarPago(@RequestParam("pagoId") long pagoId, Model model) {
        return pagosAccion("confirmar", pagoId, model);
    }

    @PostMapping("/pagos/expirar")
    public String expirarPago(@RequestParam("pagoId") long pagoId, Model model) {
        return pagosAccion("expirar", pagoId, model);
    }

    @PostMapping("/pagos/expirar-vencidos")
    public String expirarPagosVencidos(Model model) {
        return pagosAccion("expirarVencidos", null, model);
    }

    private void cargarVistaPagos(Model model) {
        model.addAttribute("pagosPendientes", plataformaService.pagosPendientesGlobales());
        model.addAttribute("pagosExpirados", plataformaService.pagosExpiradosGlobales());
        model.addAttribute("diasExpiracionManual", expiracionService.getDiasManual());
        model.addAttribute("diasExpiracionMp", expiracionService.getDiasMercadoPago());
    }
}
