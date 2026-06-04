package org.nhernandez.webapp.sistemaventas.services;

public interface CatalogoPlantillaService {

    /**
     * Importa el catalogo sugerido del rubro si el tenant aun no tiene productos.
     */
    ResultadoImportacionCatalogo importarCatalogoInicial(String tenantOwner, String tipoNegocio);
}
