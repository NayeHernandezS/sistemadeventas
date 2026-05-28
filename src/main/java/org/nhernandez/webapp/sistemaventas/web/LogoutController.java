package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class LogoutController {

    private final LoginService auth;

    public LogoutController(LoginService auth) {
        this.auth = auth;
    }

    @GetMapping("/logout")
    public void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        auth.getUsername(req).ifPresent(u -> {
            HttpSession session = req.getSession();
            session.invalidate();
        });
        resp.sendRedirect(req.getContextPath() + "/login.html");
    }
}
