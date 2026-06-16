(function () {
    'use strict';

    function panelCarro() {
        return document.getElementById('panel-carro-catalogo');
    }

    function mostrarFeedback(tipo, mensaje) {
        var contenedor = document.getElementById('catalogo-carro-feedback');
        if (!contenedor) {
            return;
        }
        contenedor.className = 'alert alert-' + tipo + ' alert-dismissible fade show py-2 small mb-3';
        contenedor.innerHTML = mensaje +
            '<button type="button" class="btn-close" aria-label="Cerrar"></button>';
        contenedor.classList.remove('d-none');
    }

    function actualizarBarraCobroMovil(data) {
        var barra = document.getElementById('caja-barra-cobro');
        if (!barra || !data) {
            return;
        }
        var totalEl = barra.querySelector('[data-caja-total]');
        var cantEl = barra.querySelector('[data-caja-cantidad]');
        var cobrar = barra.querySelector('[data-caja-cobrar]');
        if (totalEl) {
            totalEl.textContent = data.totalCarro;
        }
        if (cantEl) {
            cantEl.textContent = data.cantidadItems;
        }
        if (cobrar) {
            var vacio = !data.cantidadItems || data.cantidadItems <= 0;
            cobrar.classList.toggle('disabled', vacio);
            cobrar.setAttribute('aria-disabled', vacio ? 'true' : 'false');
        }
        actualizarBadgeNavCarro(data.cantidadItems);
    }

    function actualizarBadgeNavCarro(cantidad) {
        var badge = document.getElementById('nav-carro-badge');
        if (!badge) {
            return;
        }
        var n = cantidad || 0;
        badge.textContent = n > 99 ? '99+' : String(n);
        badge.classList.toggle('d-none', n <= 0);
    }

    function actualizarResumen(panel, data) {
        var totalEl = panel.querySelector('[data-carro-total]');
        var cantEl = panel.querySelector('[data-carro-cantidad]');
        if (totalEl) {
            totalEl.textContent = data.totalCarro;
        }
        if (cantEl) {
            cantEl.textContent = data.cantidadItems;
        }
    }

    function alternarVacio(panel, cantidadItems) {
        var vacio = panel.querySelector('[data-carro-vacio]');
        var form = panel.querySelector('[data-carro-form]');
        if (cantidadItems === 0) {
            if (vacio) {
                vacio.classList.remove('d-none');
            }
            if (form) {
                form.classList.add('d-none');
            }
            return;
        }
        if (vacio) {
            vacio.classList.add('d-none');
        }
        if (form) {
            form.classList.remove('d-none');
        }
    }

    function crearFila(data) {
        var tr = document.createElement('tr');
        tr.setAttribute('data-producto-id', String(data.productoId));
        tr.innerHTML =
            '<td class="panel-carro-nombre" title="' + escapeHtml(data.productoNombre) + '">' +
                escapeHtml(data.productoNombre) +
            '</td>' +
            '<td class="text-center">' +
                '<input type="text" class="form-control form-control-sm text-center px-1" size="2" ' +
                'name="cant_' + data.productoId + '" value="' + data.cantidadProducto + '" ' +
                'data-item-cantidad aria-label="Cantidad"/>' +
            '</td>' +
            '<td class="text-end" data-item-importe>' + data.importeLinea + '</td>' +
            '<td class="text-center">' +
                '<input type="checkbox" class="form-check-input m-0" value="' + data.productoId + '" ' +
                'name="deleteProductos" aria-label="Quitar"/>' +
            '</td>';
        return tr;
    }

    function escapeHtml(texto) {
        var div = document.createElement('div');
        div.textContent = texto || '';
        return div.innerHTML;
    }

    function actualizarPanelCarroCatalogo(data) {
        var panel = panelCarro();
        if (!panel || !data) {
            return;
        }

        actualizarResumen(panel, data);
        alternarVacio(panel, data.cantidadItems);
        actualizarBarraCobroMovil(data);

        if (!data.ok || !data.productoId) {
            return;
        }

        var tbody = panel.querySelector('[data-carro-items]');
        if (!tbody) {
            return;
        }

        var fila = tbody.querySelector('[data-producto-id="' + data.productoId + '"]');
        if (fila) {
            var cantInput = fila.querySelector('[data-item-cantidad]');
            var importeEl = fila.querySelector('[data-item-importe]');
            if (cantInput) {
                cantInput.value = data.cantidadProducto;
            }
            if (importeEl) {
                importeEl.textContent = data.importeLinea;
            }
            return;
        }

        tbody.appendChild(crearFila(data));
    }

    window.actualizarPanelCarroCatalogo = actualizarPanelCarroCatalogo;

    function urlApiAgregar(basePath) {
        return (basePath || '') + '/carro/api/agregar';
    }

    function agregarPorApi(enlace) {
        var panel = panelCarro();
        if (!panel) {
            return;
        }

        var basePath = panel.getAttribute('data-context-path') || '';
        var id = enlace.getAttribute('data-producto-id');
        if (!id || enlace.getAttribute('data-agregando') === '1') {
            return;
        }

        enlace.setAttribute('data-agregando', '1');
        enlace.classList.add('disabled');
        enlace.setAttribute('aria-disabled', 'true');

        var params = new URLSearchParams();
        params.set('id', id);
        params.set('origen', enlace.getAttribute('data-origen') || 'productos');

        fetch(urlApiAgregar(basePath) + '?' + params.toString(), {
            method: 'GET',
            headers: { 'Accept': 'application/json' },
            credentials: 'same-origin'
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('No se pudo agregar el producto.');
                }
                return response.json();
            })
            .then(function (data) {
                if (data.ok) {
                    actualizarPanelCarroCatalogo(data);
                    mostrarFeedback('success', data.mensaje + ' (Total carro: $' + data.totalCarro + ')');
                    return;
                }
                mostrarFeedback('danger', data.mensaje || 'No se pudo agregar el producto.');
            })
            .catch(function () {
                mostrarFeedback('danger', 'Error al agregar. Intenta de nuevo.');
            })
            .finally(function () {
                enlace.removeAttribute('data-agregando');
                enlace.classList.remove('disabled');
                enlace.removeAttribute('aria-disabled');
            });
    }

    function init() {
        var panel = panelCarro();
        if (!panel) {
            return;
        }

        document.addEventListener('click', function (event) {
            var boton = event.target.closest('.btn-agregar-carro');
            if (!boton) {
                return;
            }
            event.preventDefault();
            event.stopPropagation();
            agregarPorApi(boton);
        }, true);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
