(function () {
    'use strict';

    var LONGITUDES_AUTO = [8, 12, 13];
    var PAUSA_ESCANEO_MS = 120;

    function enfocarEscaneo(form) {
        var input = form.querySelector('.escaneo-codigo-input');
        if (input && document.activeElement !== input) {
            input.focus();
            input.select();
        }
    }

    function mostrarFeedback(form, tipo, mensaje) {
        var contenedor = form.querySelector('.escaneo-codigo-feedback');
        if (!contenedor) {
            return;
        }
        contenedor.className = 'escaneo-codigo-feedback alert alert-' + tipo + ' py-2 small mt-2 mb-0';
        contenedor.textContent = mensaje;
        contenedor.classList.remove('d-none');
    }

    function ocultarFeedback(form) {
        var contenedor = form.querySelector('.escaneo-codigo-feedback');
        if (contenedor) {
            contenedor.classList.add('d-none');
        }
    }

    function urlApi(form) {
        var base = form.getAttribute('data-api-url');
        if (base) {
            return base;
        }
        var action = form.getAttribute('action') || '';
        return action.replace('/agregar-por-sku', '/api/agregar-por-sku');
    }

    function enviarEscaneo(form, input) {
        var codigo = (input.value || '').trim();
        if (!codigo) {
            return;
        }

        if (form.getAttribute('data-escaneo-enviando') === '1') {
            return;
        }
        form.setAttribute('data-escaneo-enviando', '1');
        ocultarFeedback(form);

        var origen = form.querySelector('input[name="origen"]');
        var params = new URLSearchParams();
        params.set('sku', codigo);
        if (origen && origen.value) {
            params.set('origen', origen.value);
        }

        fetch(urlApi(form) + '?' + params.toString(), {
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
                    input.value = '';
                    if (origen && origen.value === 'carro') {
                        window.location.reload();
                        return;
                    }
                    if (typeof window.actualizarPanelCarroCatalogo === 'function') {
                        window.actualizarPanelCarroCatalogo(data);
                    }
                    mostrarFeedback(form, 'success', data.mensaje + ' (Total carro: $' + data.totalCarro + ')');
                    enfocarEscaneo(form);
                    return;
                }
                mostrarFeedback(form, 'danger', data.mensaje || 'Codigo no encontrado.');
                input.select();
                enfocarEscaneo(form);
            })
            .catch(function () {
                mostrarFeedback(form, 'danger', 'Error al agregar. Intenta de nuevo.');
                input.select();
                enfocarEscaneo(form);
            })
            .finally(function () {
                form.removeAttribute('data-escaneo-enviando');
            });
    }

    function initForm(form) {
        if (form.getAttribute('data-escaneo-init') === '1') {
            return;
        }
        form.setAttribute('data-escaneo-init', '1');

        var input = form.querySelector('.escaneo-codigo-input');
        if (!input) {
            return;
        }

        var timerAuto = null;

        form.addEventListener('submit', function (event) {
            event.preventDefault();
            enviarEscaneo(form, input);
        });

        input.addEventListener('keydown', function (event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                clearTimeout(timerAuto);
                enviarEscaneo(form, input);
            }
        });

        input.addEventListener('input', function () {
            clearTimeout(timerAuto);
            var codigo = (input.value || '').trim();
            if (LONGITUDES_AUTO.indexOf(codigo.length) === -1 && codigo.length < 12) {
                return;
            }
            if (codigo.length > 13) {
                return;
            }
            timerAuto = setTimeout(function () {
                if ((input.value || '').trim() === codigo) {
                    enviarEscaneo(form, input);
                }
            }, PAUSA_ESCANEO_MS);
        });

        setTimeout(function () {
            enfocarEscaneo(form);
        }, 300);
    }

    function init() {
        document.querySelectorAll('.escaneo-codigo-form').forEach(initForm);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
