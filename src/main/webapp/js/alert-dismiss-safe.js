(function () {
    'use strict';

    /**
     * Cierra alertas sin animacion de Bootstrap para evitar que el clic
     * "atraviese" y active enlaces o botones debajo (p. ej. Agregar al carro).
     */
    document.addEventListener('click', function (event) {
        var btn = event.target.closest('.alert .btn-close');
        if (!btn) {
            return;
        }
        event.preventDefault();
        event.stopPropagation();
        event.stopImmediatePropagation();

        var alertEl = btn.closest('.alert');
        if (!alertEl) {
            return;
        }
        alertEl.dispatchEvent(new CustomEvent('alert-dismissed', { bubbles: false }));
        alertEl.remove();
    }, true);
})();
