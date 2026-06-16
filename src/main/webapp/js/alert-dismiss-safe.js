(function () {
    'use strict';

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

        alertEl.dispatchEvent(new CustomEvent('alert-dismissed', { bubbles: false }));
        alertEl.remove();
    }

    function esBotonCerrarAlerta(target) {
        return target && target.closest && target.closest('.alert .btn-close');
    }

    ['pointerdown', 'click'].forEach(function (tipo) {
        document.addEventListener(tipo, function (event) {
            var btn = esBotonCerrarAlerta(event.target);
            if (btn) {
                cerrarAlerta(btn, event);
            }
        }, true);
    });
})();
