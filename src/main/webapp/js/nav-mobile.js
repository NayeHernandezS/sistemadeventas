(function () {
    'use strict';

    function initNavTenantToggle() {
        var toggler = document.getElementById('navTenantToggler');
        var menu = document.getElementById('navTenantMenu');
        if (!toggler || !menu || typeof bootstrap === 'undefined') {
            return;
        }

        var collapse = bootstrap.Collapse.getOrCreateInstance(menu, { toggle: false });

        function menuAbierto() {
            return menu.classList.contains('show');
        }

        function actualizarToggler(abierto) {
            toggler.setAttribute('aria-expanded', abierto ? 'true' : 'false');
            toggler.setAttribute('aria-label', abierto ? 'Cerrar menu' : 'Abrir menu');
        }

        toggler.addEventListener('click', function (event) {
            event.preventDefault();
            if (menuAbierto()) {
                collapse.hide();
            } else {
                collapse.show();
            }
        });

        menu.addEventListener('shown.bs.collapse', function () {
            actualizarToggler(true);
        });
        menu.addEventListener('hidden.bs.collapse', function () {
            actualizarToggler(false);
        });

        menu.querySelectorAll('.nav-link').forEach(function (link) {
            link.addEventListener('click', function () {
                if (window.matchMedia('(max-width: 991.98px)').matches && menuAbierto()) {
                    collapse.hide();
                }
            });
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initNavTenantToggle);
    } else {
        initNavTenantToggle();
    }
})();
