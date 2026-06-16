(function () {
    'use strict';

    var bloqueoClicsHasta = 0;

    function bloquearClicsBreve() {
        bloqueoClicsHasta = Date.now() + 450;
    }

    function cerrarAlerta(btn, event) {
        if (event) {
            event.preventDefault();
            event.stopPropagation();
            event.stopImmediatePropagation();
        }

        var alertEl = btn.closest('.alert');
        if (!alertEl) {
            return;
        }

        bloquearClicsBreve();
        alertEl.dispatchEvent(new CustomEvent('alert-dismissed', { bubbles: false }));
        alertEl.remove();
    }

    function esBotonCerrarAlerta(target) {
        return target && target.closest && target.closest('.alert .btn-close');
    }

    document.addEventListener('click', function (event) {
        if (Date.now() < bloqueoClicsHasta) {
            event.preventDefault();
            event.stopPropagation();
            event.stopImmediatePropagation();
            return;
        }

        var btn = esBotonCerrarAlerta(event.target);
        if (btn) {
            cerrarAlerta(btn, event);
        }
    }, true);

    document.addEventListener('pointerdown', function (event) {
        var btn = esBotonCerrarAlerta(event.target);
        if (btn) {
            cerrarAlerta(btn, event);
        }
    }, true);
})();
