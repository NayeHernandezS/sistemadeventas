package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.services.TenantLogoService;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class TenantModelAdvice {

    private final TenantLogoService tenantLogoService;

    public TenantModelAdvice(TenantLogoService tenantLogoService) {
        this.tenantLogoService = tenantLogoService;
    }

    @ModelAttribute
    public void agregarBrandingTenant(HttpServletRequest req, Model model) {
        String tenant = TenantUtil.getTenantOwner(req);
        if (tenant == null || tenant.isBlank()) {
            return;
        }
        if (tenantLogoService.tieneLogo(tenant)) {
            model.addAttribute("tenantTieneLogo", Boolean.TRUE);
            model.addAttribute("tenantLogoUrl", tenantLogoService.urlLogo(req.getContextPath()));
        }
    }
}
