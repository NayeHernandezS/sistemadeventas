package org.nhernandez.webapp.sistemaventas.services;

public interface CatalogoPlantillaService {

    /**
     * Importa el catalogo sugerido del rubro si el tenant aun no tiene productos.
     */
    ResultadoImportacionCatalogo importarCatalogoInicial(String tenantOwner, String tipoNegocio);

    /**
     * Importa servicios sugeridos del rubro si el tenant aun no tiene articulos en catalogo.
     */
    ResultadoImportacionCatalogo importarServiciosIniciales(String tenantOwner, String tipoNegocio);

    /**
     * Carga plantillas de servicio si el tenant aun no tiene ninguno (aunque ya tenga productos).
     */
    ResultadoImportacionCatalogo asegurarServiciosPlantilla(String tenantOwner, String tipoNegocio);
}
