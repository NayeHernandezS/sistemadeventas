(function () {
    'use strict';

    var deferredPrompt = null;

    function contextPath() {
        var link = document.querySelector('link[rel="manifest"]');
        if (!link || !link.getAttribute('href')) {
            return '';
        }
        var href = link.getAttribute('href');
        var idx = href.indexOf('/manifest.webmanifest');
        return idx > 0 ? href.substring(0, idx) : '';
    }

    function registrarServiceWorker() {
        if (!('serviceWorker' in navigator)) {
            return;
        }
        var base = contextPath();
        window.addEventListener('load', function () {
            navigator.serviceWorker.register(base + '/sw.js', { scope: base + '/' })
                .catch(function () {
                    /* SW opcional: la app sigue funcionando sin el */
                });
        });
    }

    function esModoStandalone() {
        return window.matchMedia('(display-mode: standalone)').matches
            || window.navigator.standalone === true;
    }

    function crearBannerInstalar() {
        if (esModoStandalone() || document.getElementById('pwa-install-banner')) {
            return;
        }

        var banner = document.createElement('div');
        banner.id = 'pwa-install-banner';
        banner.className = 'pwa-install-banner alert alert-primary alert-dismissible fade show d-none';
        banner.setAttribute('role', 'alert');
        banner.innerHTML =
            '<div class="d-flex flex-wrap align-items-center justify-content-between gap-2 w-100">' +
                '<span><i class="bi bi-phone me-1"></i> Instala Fusion Digital en tu telefono para usarla como app.</span>' +
                '<button type="button" class="btn btn-sm btn-light" id="pwa-install-btn">Instalar app</button>' +
            '</div>' +
            '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>';

        document.body.appendChild(banner);

        var btn = document.getElementById('pwa-install-btn');
        if (btn) {
            btn.addEventListener('click', function () {
                if (!deferredPrompt) {
                    return;
                }
                deferredPrompt.prompt();
                deferredPrompt.userChoice.finally(function () {
                    deferredPrompt = null;
                    banner.classList.add('d-none');
                });
            });
        }
    }

    function initInstalacion() {
        crearBannerInstalar();

        window.addEventListener('beforeinstallprompt', function (event) {
            event.preventDefault();
            deferredPrompt = event;
            var banner = document.getElementById('pwa-install-banner');
            if (banner) {
                banner.classList.remove('d-none');
            }
        });

        window.addEventListener('appinstalled', function () {
            deferredPrompt = null;
            var banner = document.getElementById('pwa-install-banner');
            if (banner) {
                banner.remove();
            }
        });
    }

    registrarServiceWorker();
    initInstalacion();
})();
