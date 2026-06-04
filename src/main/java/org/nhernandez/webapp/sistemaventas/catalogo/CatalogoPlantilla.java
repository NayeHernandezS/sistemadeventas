package org.nhernandez.webapp.sistemaventas.catalogo;

import java.util.List;

public record CatalogoPlantilla(
        String rubro,
        List<ProductoPlantilla> productos
) {
}
