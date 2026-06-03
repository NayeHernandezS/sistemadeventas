package org.nhernandez.webapp.sistemaventas.web;

import org.nhernandez.webapp.sistemaventas.services.RegistroLegalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/registro")
public class RegistroLegalController {

    private final RegistroLegalService registroLegalService;

    public RegistroLegalController(RegistroLegalService registroLegalService) {
        this.registroLegalService = registroLegalService;
    }

    @GetMapping("/terminos")
    public String terminos(Model model) {
        model.addAttribute("titulo", "Terminos de servicio");
        model.addAttribute("versionLegal", registroLegalService.versionVigente());
        return "legal/terminos";
    }

    @GetMapping("/privacidad")
    public String privacidad(Model model) {
        model.addAttribute("titulo", "Aviso de privacidad");
        model.addAttribute("versionLegal", registroLegalService.versionVigente());
        return "legal/privacidad";
    }
}
