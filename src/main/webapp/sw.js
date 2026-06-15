/* Service worker basico: cache de estaticos para uso tipo app instalada. */
'use strict';

var CACHE_VERSION = 'fusion-ventas-v2';
var PRECACHE = [
    '/css/tema.css',
    '/css/estiloindex.css',
    '/img/pwa/icon-192.png',
    '/img/pwa/icon-512.png',
    '/img/pwa/apple-touch-icon.png',
    '/img/pwa/favicon-32.png',
    '/manifest.webmanifest'
];

self.addEventListener('install', function (event) {
    event.waitUntil(
        caches.open(CACHE_VERSION).then(function (cache) {
            return cache.addAll(PRECACHE).catch(function () {
                /* Algun recurso puede fallar sin bloquear la instalacion */
            });
        }).then(function () {
            return self.skipWaiting();
        })
    );
});

self.addEventListener('activate', function (event) {
    event.waitUntil(
        caches.keys().then(function (keys) {
            return Promise.all(keys.filter(function (key) {
                return key !== CACHE_VERSION;
            }).map(function (key) {
                return caches.delete(key);
            }));
        }).then(function () {
            return self.clients.claim();
        })
    );
});

self.addEventListener('fetch', function (event) {
    var request = event.request;
    if (request.method !== 'GET') {
        return;
    }

    var url = new URL(request.url);
    if (url.origin !== self.location.origin) {
        return;
    }

    var path = url.pathname;
    var esEstatico = path.startsWith('/css/')
        || path.startsWith('/js/')
        || path.startsWith('/img/')
        || path === '/manifest.webmanifest';

    if (!esEstatico) {
        return;
    }

    event.respondWith(
        caches.match(request).then(function (cached) {
            if (cached) {
                return cached;
            }
            return fetch(request).then(function (response) {
                if (!response || response.status !== 200 || response.type === 'opaque') {
                    return response;
                }
                var clone = response.clone();
                caches.open(CACHE_VERSION).then(function (cache) {
                    cache.put(request, clone);
                });
                return response;
            });
        })
    );
});
