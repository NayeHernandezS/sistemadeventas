package org.nhernandez.webapp.sistemaventas.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.nhernandez.webapp.sistemaventas.support.IntegrationTestBase;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Tag("integration")
class InventarioAjusteIntegracionTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    private static RequestPostProcessor sesionTenant(String username, String rol, String tenantOwner) {
        return request -> {
            var session = request.getSession(true);
            session.setAttribute("username", username);
            session.setAttribute("rol", rol);
            session.setAttribute(TenantUtil.SESSION_TENANT, tenantOwner);
            return request;
        };
    }

    @Test
    void ajusteGet_muestraFormularioParaAdmin() throws Exception {
        withJdbc(() -> mockMvc.perform(get("/inventario/ajuste")
                        .param("id", "1")
                        .with(user("tienda1").roles("ADMIN"))
                        .with(sesionTenant("tienda1", "ADMIN", "tienda1")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("inventarioAjuste")));
    }

    @Test
    void ajustePost_registraEntrada() throws Exception {
        withJdbc(() -> mockMvc.perform(post("/inventario/ajuste")
                        .param("productoId", "1")
                        .param("tipo", "ENTRADA")
                        .param("cantidad", "3")
                        .param("motivo", "Compra prueba")
                        .with(csrf())
                        .with(user("tienda1").roles("ADMIN"))
                        .with(sesionTenant("tienda1", "ADMIN", "tienda1")))
                .andDo(print())
                .andExpect(status().is3xxRedirection()));
    }
}
