package org.nhernandez.webapp.sistemaventas.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegacyPathsController {

    @GetMapping("/login.jsp")
    public String loginJsp() {
        return "redirect:/login";
    }
}
