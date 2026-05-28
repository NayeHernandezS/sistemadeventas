package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index(HttpSession session) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }
        return "index";
    }

    @GetMapping("/index.jsp")
    public String indexLegacy() {
        return "redirect:/";
    }
}
