(function () {
    'use strict';

    function normalizar(texto) {
        return (texto || '')
            .toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '');
    }

    function filasBuscables(tabla) {
        return tabla.querySelectorAll('tbody tr[data-fila-busqueda]');
    }

    function textoFila(fila) {
        const attr = fila.getAttribute('data-buscar');
        if (attr) {
            return normalizar(attr);
        }
        return normalizar(fila.textContent);
    }

    function etiqueta(input) {
        return input.getAttribute('data-etiqueta') || 'registro';
    }

    function pluralEtiqueta(cantidad, base) {
        return cantidad + ' ' + base + (cantidad === 1 ? '' : 's');
    }

    function actualizarContador(tablaId, visibles, total, filtrando, input) {
        const contador = document.querySelector('.buscador-tabla-contador[data-tabla-id="' + tablaId + '"]');
        if (!contador) {
            return;
        }
        const base = input ? etiqueta(input) : 'registro';
        if (total === 0) {
            contador.textContent = 'Sin ' + base + 's';
            return;
        }
        if (filtrando) {
            contador.textContent = 'Mostrando ' + visibles + ' de ' + total;
            return;
        }
        contador.textContent = pluralEtiqueta(total, base);
    }

    function alternarVacio(tablaId, mostrar) {
        const vacio = document.querySelector('.buscador-tabla-vacio[data-tabla-id="' + tablaId + '"]');
        if (vacio) {
            vacio.classList.toggle('d-none', !mostrar);
        }
    }

    function filtrar(input) {
        const tablaId = input.getAttribute('data-tabla-id');
        const tabla = document.getElementById(tablaId);
        if (!tabla) {
            return;
        }

        const query = normalizar(input.value.trim());
        const filas = filasBuscables(tabla);
        let visibles = 0;

        filas.forEach(function (fila) {
            const coincide = !query || textoFila(fila).includes(query);
            fila.classList.toggle('d-none', !coincide);
            if (coincide) {
                visibles++;
            }
        });

        actualizarContador(tablaId, visibles, filas.length, query.length > 0, input);
        alternarVacio(tablaId, query.length > 0 && visibles === 0);
    }

    function enlazarInput(input) {
        if (input.getAttribute('data-buscador-init') === '1') {
            return;
        }
        input.setAttribute('data-buscador-init', '1');
        input.addEventListener('input', function () {
            filtrar(input);
        });
        filtrar(input);
    }

    function init() {
        document.querySelectorAll('.buscador-tabla-input').forEach(enlazarInput);

        document.querySelectorAll('.buscador-tabla-limpiar').forEach(function (btn) {
            if (btn.getAttribute('data-buscador-init') === '1') {
                return;
            }
            btn.setAttribute('data-buscador-init', '1');
            btn.addEventListener('click', function () {
                const tablaId = btn.getAttribute('data-tabla-id');
                const input = document.querySelector('.buscador-tabla-input[data-tabla-id="' + tablaId + '"]');
                if (!input) {
                    return;
                }
                input.value = '';
                filtrar(input);
                input.focus();
            });
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
