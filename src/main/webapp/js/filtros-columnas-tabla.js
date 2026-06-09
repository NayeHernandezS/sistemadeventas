(function () {
    'use strict';

    function storageKey(wrap) {
        var tablaId = wrap.getAttribute('data-tabla-id') || 'tabla';
        var usuario = wrap.getAttribute('data-usuario') || 'anon';
        var tenant = wrap.getAttribute('data-tenant') || 'local';
        return 'sistema-ventas.filtros-columnas.' + tenant + '.' + usuario + '.' + tablaId;
    }

    function columnasDisponibles(wrap) {
        return Array.from(wrap.querySelectorAll('.filtros-columnas-check')).map(function (input) {
            return input.getAttribute('data-col');
        });
    }

    function aplicarVisibilidad(tabla, columnasVisibles) {
        tabla.querySelectorAll('[data-col]').forEach(function (celda) {
            const col = celda.getAttribute('data-col');
            celda.classList.toggle('d-none', columnasVisibles.indexOf(col) === -1);
        });
    }

    function leerSeleccion(wrap) {
        const visibles = [];
        wrap.querySelectorAll('.filtros-columnas-check').forEach(function (input) {
            if (input.checked) {
                visibles.push(input.getAttribute('data-col'));
            }
        });
        return visibles;
    }

    function guardarSeleccion(wrap, visibles) {
        try {
            localStorage.setItem(storageKey(wrap), JSON.stringify(visibles));
        } catch (e) {
            /* sin almacenamiento */
        }
    }

    function cargarSeleccion(wrap) {
        try {
            const raw = localStorage.getItem(storageKey(wrap));
            if (!raw) {
                return null;
            }
            const parsed = JSON.parse(raw);
            return Array.isArray(parsed) ? parsed : null;
        } catch (e) {
            return null;
        }
    }

    function sincronizarCheckboxes(wrap, visibles) {
        wrap.querySelectorAll('.filtros-columnas-check').forEach(function (input) {
            const col = input.getAttribute('data-col');
            input.checked = visibles.indexOf(col) !== -1;
        });
    }

    function actualizarEstado(wrap) {
        const tablaId = wrap.getAttribute('data-tabla-id');
        const tabla = document.getElementById(tablaId);
        if (!tabla) {
            return;
        }

        const visibles = leerSeleccion(wrap);
        if (visibles.length === 0) {
            const primero = wrap.querySelector('.filtros-columnas-check');
            if (primero) {
                primero.checked = true;
                visibles.push(primero.getAttribute('data-col'));
            }
        }

        aplicarVisibilidad(tabla, visibles);
        guardarSeleccion(wrap, visibles);
        actualizarIndicador(wrap, visibles);
    }

    function actualizarIndicador(wrap, visibles) {
        const indicador = wrap.querySelector('.filtros-columnas-indicador');
        if (!indicador) {
            return;
        }
        const total = wrap.querySelectorAll('.filtros-columnas-check').length;
        const ocultas = total - visibles.length;
        indicador.textContent = ocultas > 0 ? ocultas + ' columna(s) oculta(s)' : 'Todas las columnas visibles';
        indicador.classList.toggle('text-primary', ocultas > 0);
    }

    function restaurarTodas(wrap) {
        wrap.querySelectorAll('.filtros-columnas-check').forEach(function (input) {
            input.checked = true;
        });
        actualizarEstado(wrap);
    }

    function initWrap(wrap) {
        if (wrap.getAttribute('data-filtros-init') === '1') {
            return;
        }
        wrap.setAttribute('data-filtros-init', '1');

        const tablaId = wrap.getAttribute('data-tabla-id');
        const guardado = cargarSeleccion(wrap);
        const disponibles = columnasDisponibles(wrap);

        if (guardado) {
            const validas = guardado.filter(function (col) {
                return disponibles.indexOf(col) !== -1;
            });
            if (validas.length > 0) {
                sincronizarCheckboxes(wrap, validas);
            }
        }

        wrap.querySelectorAll('.filtros-columnas-check').forEach(function (input) {
            input.addEventListener('change', function () {
                const visibles = leerSeleccion(wrap);
                if (visibles.length === 0) {
                    input.checked = true;
                    return;
                }
                actualizarEstado(wrap);
            });
        });

        const btnRestaurar = wrap.querySelector('.filtros-columnas-restaurar');
        if (btnRestaurar) {
            btnRestaurar.addEventListener('click', function (event) {
                event.preventDefault();
                event.stopPropagation();
                restaurarTodas(wrap);
            });
        }

        const panel = wrap.querySelector('.filtros-columnas-panel');
        if (panel) {
            panel.addEventListener('click', function (event) {
                event.stopPropagation();
            });
        }

        actualizarEstado(wrap);
    }

    function init() {
        document.querySelectorAll('.filtros-columnas-wrap').forEach(initWrap);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
