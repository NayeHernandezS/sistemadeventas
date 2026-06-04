package org.nhernandez.webapp.sistemaventas.catalogo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nhernandez.webapp.sistemaventas.util.TipoNegocioUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;

public final class CatalogoPlantillaLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String RUTA_BASE = "catalogos/";

    private CatalogoPlantillaLoader() {
    }

    public static Optional<CatalogoPlantilla> cargar(String tipoNegocio) {
        String rubro = normalizarRubro(tipoNegocio);
        Optional<CatalogoPlantilla> catalogo = leerArchivo(rubro);
        if (catalogo.isPresent()) {
            return catalogo;
        }
        if (!"otro".equals(rubro)) {
            return leerArchivo("otro");
        }
        return Optional.empty();
    }

    private static Optional<CatalogoPlantilla> leerArchivo(String rubro) {
        String recurso = RUTA_BASE + rubro + ".json";
        try (InputStream in = CatalogoPlantillaLoader.class.getClassLoader().getResourceAsStream(recurso)) {
            if (in == null) {
                return Optional.empty();
            }
            return Optional.of(MAPPER.readValue(in, CatalogoPlantilla.class));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el catalogo plantilla: " + recurso, e);
        }
    }

    private static String normalizarRubro(String tipoNegocio) {
        if (tipoNegocio == null || tipoNegocio.isBlank()) {
            return "otro";
        }
        String codigo = tipoNegocio.trim().toLowerCase(Locale.ROOT);
        return TipoNegocioUtil.esValido(codigo) ? codigo : "otro";
    }
}
