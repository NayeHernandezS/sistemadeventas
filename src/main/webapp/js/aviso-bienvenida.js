(function () {
    const alertEl = document.getElementById('aviso-bienvenida');
    if (!alertEl) {
        return;
    }

    const username = alertEl.dataset.username;
    if (!username) {
        return;
    }

    const storageKey = 'sistema-ventas.aviso-bienvenida.' + username;

    function marcarCerrado() {
        try {
            localStorage.setItem(storageKey, '1');
        } catch (e) {
            /* sin almacenamiento */
        }
    }

    if (localStorage.getItem(storageKey) === '1') {
        alertEl.remove();
        return;
    }

    alertEl.classList.remove('d-none');
    alertEl.addEventListener('alert-dismissed', marcarCerrado);
})();
