/* Leaflet 1.9.4. El mapa consulta Spring Boot; no se conecta directamente a Supabase. */
(function (global) {
    'use strict';

    function esUbicable(centro) {
        return centro != null && centro.ubicable === true
            && Number.isFinite(centro.latitud) && Number.isFinite(centro.longitud)
            && Math.abs(centro.latitud) <= 90 && Math.abs(centro.longitud) <= 180;
    }

    // Las dependencias opcionales permiten probar el comportamiento sin servicios externos.
    async function iniciarMapa(entorno = global) {
        const doc = entorno.document;
        const lienzo = doc.getElementById('mapa-centros');
        if (!lienzo || lienzo.dataset.iniciado === 'true') return null;
        const estado = doc.getElementById('mapa-estado');
        const aviso = doc.getElementById('mapa-aviso');
        const encuadrar = doc.getElementById('mapa-encuadrar');
        const reintentar = doc.getElementById('mapa-reintentar');
        const login = doc.getElementById('mapa-login');
        const L = entorno.L;

        if (!L) {
            estado.textContent = 'No se pudo cargar el mapa. Revisa tu conexión; la lista de centros sigue disponible.';
            lienzo.setAttribute('aria-busy', 'false');
            reintentar.hidden = false;
            reintentar.onclick = () => entorno.location.reload();
            return null;
        }

        let mapa;
        try {
            // Vista general de Ciudad de Guatemala; no representa una ubicación exacta de la UVG.
            mapa = L.map(lienzo, { scrollWheelZoom: false }).setView([14.60, -90.50], 12);
        } catch (error) {
            estado.textContent = 'No se pudo iniciar el mapa. Puedes consultar la lista de centros.';
            lienzo.setAttribute('aria-busy', 'false');
            return null;
        }
        lienzo.dataset.iniciado = 'true';
        const capa = L.tileLayer(lienzo.dataset.tilesUrl, {
            maxZoom: 19,
            referrerPolicy: 'strict-origin-when-cross-origin',
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        });
        let erroresTeselas = 0;
        capa.on('loading', () => { erroresTeselas = 0; });
        capa.on('tileerror', () => {
            erroresTeselas++;
            aviso.hidden = false;
            aviso.textContent = 'El fondo del mapa no pudo cargarse por completo. Revisa tu conexión o consulta las direcciones de la lista.';
        });
        capa.on('load', () => { if (erroresTeselas === 0) aviso.hidden = true; });
        capa.addTo(mapa);
        const grupo = L.featureGroup().addTo(mapa);
        let puntos = [];

        function ajustarVista() {
            if (puntos.length === 0) return;
            mapa.invalidateSize();
            if (puntos.length === 1) {
                mapa.setView([puntos[0].latitud, puntos[0].longitud], 15);
            } else {
                mapa.fitBounds(grupo.getBounds(), { padding: [28, 28], maxZoom: 16 });
            }
        }
        encuadrar.onclick = ajustarVista;

        async function cargarCentros() {
            reintentar.hidden = true;
            reintentar.disabled = true;
            login.hidden = true;
            encuadrar.disabled = true;
            lienzo.setAttribute('aria-busy', 'true');
            estado.textContent = 'Cargando centros…';
            grupo.clearLayers();
            puntos = [];
            const cancelacion = new entorno.AbortController();
            const timeout = entorno.setTimeout(() => cancelacion.abort(), 12000);
            try {
                const respuesta = await entorno.fetch(lienzo.dataset.apiUrl, {
                    credentials: 'same-origin',
                    headers: { Accept: 'application/json' },
                    signal: cancelacion.signal
                });
                if (respuesta.redirected || respuesta.status === 401 || respuesta.status === 403) {
                    login.hidden = false;
                    throw new Error('Tu sesión puede haber terminado. Vuelve a iniciar sesión para cargar los centros.');
                }
                if (!respuesta.ok) throw new Error('No se pudieron consultar los centros. Intenta nuevamente.');
                if (!(respuesta.headers.get('content-type') || '').includes('application/json')) {
                    throw new Error('La respuesta del servidor no contiene datos del mapa. Revisa que estés ejecutando la versión actual del proyecto.');
                }
                const centros = await respuesta.json();
                if (!Array.isArray(centros)) throw new Error('El servidor devolvió datos inesperados para el mapa.');
                puntos = centros.filter(esUbicable);
                puntos.forEach(centro => {
                    const nombre = typeof centro.nombre === 'string' ? centro.nombre : 'Centro de reciclaje';
                    // Leaflet interpreta strings como HTML: se usa textContent para datos de la base.
                    const etiqueta = doc.createElement('span');
                    etiqueta.textContent = nombre;
                    L.marker([centro.latitud, centro.longitud], {
                        title: nombre,
                        alt: nombre,
                        keyboard: true
                    }).bindTooltip(etiqueta).addTo(grupo);
                });
                const sinUbicacion = centros.length - puntos.length;
                if (centros.length === 0) {
                    estado.textContent = 'No hay centros para mostrar con esta búsqueda. Prueba otros filtros.';
                } else if (puntos.length === 0) {
                    estado.textContent = 'Los centros encontrados aún no tienen coordenadas válidas. Consulta sus direcciones en la lista.';
                } else {
                    estado.textContent = puntos.length === 1 ? '1 centro ubicado en el mapa.' : `${puntos.length} centros ubicados en el mapa.`;
                    if (sinUbicacion > 0) estado.textContent += ` Sin ubicación válida: ${sinUbicacion}. Puedes consultarlos en la lista.`;
                    encuadrar.disabled = false;
                    ajustarVista();
                }
            } catch (error) {
                grupo.clearLayers();
                puntos = [];
                encuadrar.disabled = true;
                estado.textContent = error.name === 'AbortError'
                    ? 'La consulta tardó demasiado. Revisa tu conexión e intenta nuevamente.'
                    : error instanceof TypeError
                        ? 'No se pudo conectar con el servidor. Puedes consultar la lista y reintentar.'
                        : error.message;
                reintentar.hidden = false;
            } finally {
                entorno.clearTimeout(timeout);
                reintentar.disabled = false;
                lienzo.setAttribute('aria-busy', 'false');
            }
        }
        reintentar.onclick = cargarCentros;
        await cargarCentros();
        return { mapa, cargarCentros, ajustarVista };
    }

    if (typeof module !== 'undefined' && module.exports) {
        module.exports = { esUbicable, iniciarMapa };
    } else {
        global.EcoUVGMapa = { esUbicable, iniciarMapa };
        if (global.document.readyState === 'loading') {
            global.document.addEventListener('DOMContentLoaded', () => iniciarMapa());
        } else {
            iniciarMapa();
        }
    }
})(globalThis);
