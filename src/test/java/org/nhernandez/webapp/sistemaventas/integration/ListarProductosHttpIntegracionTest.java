package org.nhernandez.webapp.sistemaventas.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.nhernandez.webapp.sistemaventas.support.IntegrationTestBase;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Tag("integration")
class ListarProductosHttpIntegracionTest extends IntegrationTestBase {

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
    void productos_sinSesion_redirigeALogin() throws Exception {
        mockMvc.perform(get("/productos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login**"));
    }

    @Test
    @WithMockUser(username = "tienda1", roles = "ADMIN")
    void productos_adminTenant_respondeOkSinError500() throws Exception {
        mockMvc.perform(get("/productos")
                        .with(user("tienda1").roles("ADMIN"))
                        .with(sesionTenant("tienda1", "ADMIN", "tienda1")))
                .andExpect(status().isOk())
                .andExpect(view().name("listar"));
    }

    @Test
    @WithMockUser(username = "tienda1", roles = "ADMIN")
    void carroApiAgregar_productoValido_noDevuelve500() throws Exception {
        mockMvc.perform(get("/carro/api/agregar")
                        .param("id", "1")
                        .param("origen", "productos")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(user("tienda1").roles("ADMIN"))
                        .with(sesionTenant("tienda1", "ADMIN", "tienda1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
    }

    @Test
    @WithMockUser(username = "tienda1", roles = "ADMIN")
    void carroApiAgregar_productoInvalido_noDevuelve500() throws Exception {
        mockMvc.perform(get("/carro/api/agregar")
                        .param("id", "99999")
                        .param("origen", "productos")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(user("tienda1").roles("ADMIN"))
                        .with(sesionTenant("tienda1", "ADMIN", "tienda1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false));
    }

    @Test
    @WithMockUser(username = "tienda1", roles = "ADMIN")
    void carroAgregar_get_redirectSinError500() throws Exception {
        mockMvc.perform(get("/carro/agregar")
                        .param("id", "1")
                        .param("origen", "productos")
                        .with(user("tienda1").roles("ADMIN"))
                        .with(sesionTenant("tienda1", "ADMIN", "tienda1")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/productos#catalogo"));
    }
}
