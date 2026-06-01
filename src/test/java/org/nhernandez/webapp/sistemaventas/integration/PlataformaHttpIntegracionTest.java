package org.nhernandez.webapp.sistemaventas.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.nhernandez.webapp.sistemaventas.support.IntegrationTestBase;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Tag("integration")
class PlataformaHttpIntegracionTest extends IntegrationTestBase {

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
    void plataformaClientes_redirigeALoginSinSesion() throws Exception {
        mockMvc.perform(get("/plataforma/clientes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login**"));
    }

    @Test
    @WithMockUser(username = "tienda1", roles = "ADMIN")
    void plataformaClientes_rechazaAdminTenant() throws Exception {
        mockMvc.perform(get("/plataforma/clientes")
                        .with(sesionTenant("tienda1", "ADMIN", "tienda1")))
                .andExpect(status().isForbidden());
    }

    @Test
    void plataformaClientes_permiteSuperAdmin() throws Exception {
        mockMvc.perform(get("/plataforma/clientes")
                        .with(user("plataforma").roles("SUPER_ADMIN"))
                        .with(sesionTenant("plataforma", "SUPER_ADMIN", "plataforma")))
                .andExpect(status().isOk())
                .andExpect(view().name("plataforma/clientes"));
    }

    @Test
    void suscripcion_permiteAdminTenant() throws Exception {
        mockMvc.perform(get("/suscripcion")
                        .with(user("tienda1").roles("ADMIN"))
                        .with(sesionTenant("tienda1", "ADMIN", "tienda1")))
                .andExpect(status().isOk())
                .andExpect(view().name("suscripcion"));
    }
}
