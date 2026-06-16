(function () {
    'use strict';

    function normalizar(texto) {
        return (texto || '').toLowerCase().trim();
    }

    function filtrarProductos(termino) {
        var cards = document.querySelectorAll('[data-caja-producto]');
        var visible = 0;
        var busqueda = normalizar(termino);

        cards.forEach(function (card) {
            var texto = normalizar(card.getAttribute('data-buscar'));
            var coincide = !busqueda || texto.indexOf(busqueda) !== -1;
            card.classList.toggle('d-none', !coincide);
            if (coincide) {
                visible += 1;
            }
        });

        var sinResultados = document.getElementById('caja-sin-resultados');
        if (sinResultados) {
            sinResultados.classList.toggle('d-none', visible > 0);
        }
    }

    function initBusqueda() {
        var input = document.getElementById('caja-buscar');
        if (!input) {
            return;
        }
        input.addEventListener('input', function () {
            filtrarProductos(input.value);
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initBusqueda);
    } else {
        initBusqueda();
    }
})();
