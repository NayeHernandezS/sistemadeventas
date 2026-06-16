package org.nhernandez.webapp.sistemaventas.util;

import org.nhernandez.webapp.sistemaventas.models.ListaCompraHoy;
import org.nhernandez.webapp.sistemaventas.models.ProductoCompraSugerida;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class ListaCompraCsvExporter {

    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    public byte[] exportar(ListaCompraHoy lista) {
        StringBuilder sb = new StringBuilder();
        sb.append("Lista comprar hoy\n");
        sb.append("Fecha,").append(ReporteCsvExporter.escapar(lista.getFecha())).append('\n');
        sb.append("Umbral stock bajo,").append(lista.getStockMinimo()).append('\n');
        sb.append("Productos,").append(lista.getTotalProductos()).append('\n');
        sb.append("Unidades sugeridas,").append(lista.getTotalUnidadesSugeridas()).append('\n');
        sb.append('\n');
        sb.append("Producto,SKU,Categoria,Existencias,Alerta,Comprar (uds),Vendidas 7d,Precio compra,Costo estimado\n");
        for (ProductoCompraSugerida p : lista.getProductos()) {
            sb.append(ReporteCsvExporter.escapar(p.getNombre())).append(',');
            sb.append(ReporteCsvExporter.escapar(p.getSku())).append(',');
            sb.append(ReporteCsvExporter.escapar(p.getCategoria())).append(',');
            sb.append(p.getExistencias()).append(',');
            sb.append(ReporteCsvExporter.escapar(etiquetaAlerta(p))).append(',');
            sb.append(p.getCantidadSugerida()).append(',');
            sb.append(p.getUnidadesVendidas7d()).append(',');
            sb.append(p.getPrecioCompra()).append(',');
            sb.append(p.getCostoEstimadoReposicion()).append('\n');
        }
        byte[] contenido = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] conBom = new byte[UTF8_BOM.length + contenido.length];
        System.arraycopy(UTF8_BOM, 0, conBom, 0, UTF8_BOM.length);
        System.arraycopy(contenido, 0, conBom, UTF8_BOM.length, contenido.length);
        return conBom;
    }

    private static String etiquetaAlerta(ProductoCompraSugerida p) {
        return p.isAgotado() ? "Agotado" : "Stock bajo";
    }
}
