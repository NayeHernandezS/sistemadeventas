package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.services.TenantLogoService;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.concurrent.TimeUnit;

@Controller
public class TenantLogoController {

    private final TenantLogoService tenantLogoService;

    public TenantLogoController(TenantLogoService tenantLogoService) {
        this.tenantLogoService = tenantLogoService;
    }

    @GetMapping("/tenant/logo")
    public ResponseEntity<org.springframework.core.io.Resource> logo(HttpServletRequest req) {
        String tenant = TenantUtil.getTenantOwner(req);
        if (tenant == null || tenant.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        return tenantLogoService.obtenerLogo(tenant)
                .map(logo -> ResponseEntity.ok()
                        .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                        .contentType(MediaType.parseMediaType(logo.contentType()))
                        .body(logo.resource()))
                .orElse(ResponseEntity.notFound().build());
    }
}
