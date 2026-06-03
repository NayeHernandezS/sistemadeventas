package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.legal.DocumentosLegales;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class RegistroLegalService {

    public String versionVigente() {
        return DocumentosLegales.VERSION_VIGENTE;
    }

    public Map<String, String> validarAceptacion(String aceptaTerminos, String aceptaPrivacidad) {
        Map<String, String> errores = new HashMap<>();
        if (!checkboxMarcado(aceptaTerminos)) {
            errores.put("aceptaTerminos", "Debes aceptar los Terminos de servicio");
        }
        if (!checkboxMarcado(aceptaPrivacidad)) {
            errores.put("aceptaPrivacidad", "Debes aceptar el Aviso de privacidad");
        }
        return errores;
    }

    public LocalDateTime momentoAceptacion() {
        return LocalDateTime.now();
    }

    private static boolean checkboxMarcado(String valor) {
        return "1".equals(valor) || "on".equalsIgnoreCase(valor) || "true".equalsIgnoreCase(valor);
    }
}
