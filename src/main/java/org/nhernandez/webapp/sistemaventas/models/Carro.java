package org.nhernandez.webapp.sistemaventas.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@SessionScope
public class Carro implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(Carro.class);

    private List<ItemCarro> items;

    public Carro() {
        this.items = new ArrayList<>();
    }

    @jakarta.annotation.PostConstruct
    public void inicializar() {
        this.items = new ArrayList<>();
        log.info("inicializando el carro de compras");
    }

    @jakarta.annotation.PreDestroy
    public void destruir() {
        log.info("destruyendo el carro de compras");
    }

    public void addItemCarro(ItemCarro itemCarro) {
        if (items.contains(itemCarro)) {
            Optional<ItemCarro> optionalItemCarro = items.stream()
                    .filter(i -> i.equals(itemCarro))
                    .findAny();
            optionalItemCarro.ifPresent(i -> i.setCantidad(i.getCantidad() + 1));
        } else {
            this.items.add(itemCarro);
        }
    }

    public List<ItemCarro> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getTotal() {
        return items.stream().mapToInt(ItemCarro::getImporte).sum();
    }

    public void removeProductos(List<String> productoIds) {
        if (productoIds != null) {
            productoIds.forEach(this::removeProducto);
        }
    }

    public void removeProducto(String productoId) {
        Optional<ItemCarro> producto = findProducto(productoId);
        producto.ifPresent(items::remove);
    }

    public void updateCantidad(String productoId, int cantidad) {
        Optional<ItemCarro> producto = findProducto(productoId);
        producto.ifPresent(itemCarro -> itemCarro.setCantidad(cantidad));
    }

    private Optional<ItemCarro> findProducto(String productoId) {
        return items.stream()
                .filter(itemCarro -> productoId.equals(Long.toString(itemCarro.getProducto().getId())))
                .findAny();
    }

    public void vaciar() {
        items.clear();
    }
}
