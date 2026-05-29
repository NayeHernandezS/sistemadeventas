package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index(HttpServletRequest req) {
        if (RolUtil.esSuperAdmin(req)) {
            return "redirect:/plataforma";
        }
        return "index";
    }

    @GetMapping("/index.jsp")
    public String indexLegacy() {
        return "redirect:/";
    }
}
