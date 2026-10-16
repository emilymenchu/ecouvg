/* Leaflet 1.9.4. El mapa consulta Spring Boot; no se conecta directamente a Supabase. */
(function (global) {
    'use strict';

    function esUbicable(centro) {
        return centro != null && centro.ubicable === true
            && Number.isFinite(centro.latitud) && Number.isFinite(centro.longitud)
            && Math.abs(centro.latitud) <= 90 && Math.abs(centro.longitud) <= 180;
    }

    function identificadorCentro(id) {
        return Number.isSafeInteger(id) && id > 0 ? String(id) : null;
    }

    function crearFicha(doc, centro, detalleBase, verTarjeta, mostrarEnlace = true) {
        const ficha = doc.createElement('section');
        ficha.className = 'mapa-ficha';
        function texto(tag, valor) {
            const elemento = doc.createElement(tag);
            elemento.textContent = valor;
            ficha.appendChild(elemento);
            return elemento;
        }
        texto('h3', centro.nombre || 'Centro de reciclaje');
        texto('p', centro.ubicacion || 'Dirección por confirmar');
        texto('p', 'Horario: ' + (centro.horario || 'Por confirmar con el centro'));
        const materiales = Array.isArray(centro.materiales)
            ? centro.materiales.filter(m => typeof m === 'string' && m.trim()) : [];
        texto('p', 'Recibe: ' + (materiales.length ? materiales.join(', ') : 'Materiales por confirmar'));
        const id = identificadorCentro(centro.id);
        if (id !== null && mostrarEnlace) {
            const enlace = texto('a', 'Ver información completa');
            enlace.href = detalleBase + id;
        }
        if (verTarjeta) {
            const boton = texto('button', 'Ver tarjeta en la lista');
            boton.type = 'button';
            boton.onclick = verTarjeta;
        }
        return ficha;
    }

    // Las dependencias opcionales permiten probar el comportamiento sin servicios externos.
    async function iniciarMapa(entorno = global) {
        const doc = entorno.document;
        const lienzo = doc.getElementById('mapa-centros');
        if (!lienzo || lienzo.dataset.iniciado === 'true') return null;
        const detalle = lienzo.dataset.modo === 'detalle';
        if (detalle) lienzo.hidden = true;
        const estado = doc.getElementById('mapa-estado');
        const aviso = doc.getElementById('mapa-aviso');
        const encuadrar = doc.getElementById('mapa-encuadrar');
        const reintentar = doc.getElementById('mapa-reintentar');
        const login = doc.getElementById('mapa-login');
        const L = entorno.L;
        const botones = Array.from(doc.querySelectorAll('[data-ver-centro]'));
        const tarjetas = new Map(Array.from(doc.querySelectorAll('[data-centro-tarjeta]'))
            .map(t => [t.dataset.centroTarjeta, t]));
        function desactivarBotones(mensaje) {
            botones.forEach(b => { b.disabled = true; b.title = mensaje; b.setAttribute('aria-pressed', 'false'); });
        }
        desactivarBotones('Esperando la carga del mapa');

        if (!L) {
            desactivarBotones('El mapa no está disponible');
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
            desactivarBotones('El mapa no está disponible');
            estado.textContent = 'No se pudo iniciar el mapa. Puedes consultar la dirección del centro.';
            reintentar.hidden = false;
            reintentar.onclick = () => entorno.location.reload();
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
        const marcadores = new Map();
        function resaltarTarjeta(id) {
            tarjetas.forEach((tarjeta, clave) => tarjeta.classList.toggle('tarjeta-seleccionada', clave === id));
            botones.forEach(b => b.setAttribute('aria-pressed', String(b.dataset.verCentro === id)));
        }
        function desplazar(elemento) {
            const reducido = entorno.matchMedia && entorno.matchMedia('(prefers-reduced-motion: reduce)').matches;
            elemento.scrollIntoView({ behavior: reducido ? 'auto' : 'smooth', block: 'center' });
            elemento.focus({ preventScroll: true });
        }
        function seleccionarCentro(id) {
            const marcador = marcadores.get(String(id));
            if (!marcador) return false;
            mapa.invalidateSize();
            mapa.setView(marcador.getLatLng(), 16);
            marcador.openPopup();
            resaltarTarjeta(String(id));
            desplazar(lienzo);
            return true;
        }
        botones.forEach(b => { b.onclick = () => seleccionarCentro(b.dataset.verCentro); });

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
            if (detalle) lienzo.hidden = true;
            estado.textContent = detalle ? 'Cargando ubicación…' : 'Cargando centros…';
            grupo.clearLayers();
            puntos = [];
            marcadores.clear();
            resaltarTarjeta(null);
            desactivarBotones('Esperando la carga del mapa');
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
                if (detalle && respuesta.status === 404) throw new Error('Este centro ya no está disponible. Vuelve al directorio de centros.');
                if (!respuesta.ok) throw new Error('No se pudieron consultar los centros. Intenta nuevamente.');
                if (!(respuesta.headers.get('content-type') || '').includes('application/json')) {
                    throw new Error('La respuesta del servidor no contiene datos del mapa. Revisa que estés ejecutando la versión actual del proyecto.');
                }
                const datos = await respuesta.json();
                if (detalle && (!datos || Array.isArray(datos) || typeof datos !== 'object'
                    || identificadorCentro(datos.id) !== lienzo.dataset.centroId)) {
                    throw new Error('El servidor devolvió datos inesperados para este centro.');
                }
                const centros = detalle ? [datos] : datos;
                if (!Array.isArray(centros)) throw new Error('El servidor devolvió datos inesperados para el mapa.');
                puntos = centros.filter(esUbicable);
                puntos.forEach(centro => {
                    const nombre = typeof centro.nombre === 'string' ? centro.nombre : 'Centro de reciclaje';
                    // Leaflet interpreta strings como HTML: se usa textContent para datos de la base.
                    const etiqueta = doc.createElement('span');
                    etiqueta.textContent = nombre;
                    const id = identificadorCentro(centro.id);
                    const tarjeta = id === null ? null : tarjetas.get(id);
                    const ficha = crearFicha(doc, centro, lienzo.dataset.detalleBase || '/centros/',
                        tarjeta ? () => {
                            mapa.closePopup();
                            resaltarTarjeta(id);
                            desplazar(tarjeta);
                        } : null, !detalle);
                    const marcador = L.marker([centro.latitud, centro.longitud], {
                        title: nombre,
                        alt: nombre,
                        keyboard: true
                    }).bindTooltip(etiqueta)
                        .bindPopup(ficha, { maxWidth: 290, maxHeight: 230 })
                        .on('popupopen', () => resaltarTarjeta(id))
                        .on('popupclose', () => resaltarTarjeta(null))
                        .addTo(grupo);
                    if (id !== null) marcadores.set(id, marcador);
                });
                botones.forEach(b => {
                    b.disabled = !marcadores.has(b.dataset.verCentro);
                    b.title = b.disabled ? 'Este centro no tiene una ubicación disponible en el mapa'
                        : 'Centrar el mapa y abrir la información del centro';
                });
                const sinUbicacion = centros.length - puntos.length;
                if (centros.length === 0) {
                    estado.textContent = 'No hay centros para mostrar con esta búsqueda. Prueba otros filtros.';
                } else if (puntos.length === 0) {
                    estado.textContent = detalle
                        ? 'Este centro aún no tiene coordenadas válidas. Consulta la dirección indicada arriba.'
                        : 'Los centros encontrados aún no tienen coordenadas válidas. Consulta sus direcciones en la lista.';
                } else {
                    estado.textContent = puntos.length === 1 ? '1 centro ubicado en el mapa.' : `${puntos.length} centros ubicados en el mapa.`;
                    if (sinUbicacion > 0) estado.textContent += ` Sin ubicación válida: ${sinUbicacion}. Puedes consultarlos en la lista.`;
                    encuadrar.disabled = false;
                    lienzo.hidden = false;
                    ajustarVista();
                }
            } catch (error) {
                grupo.clearLayers();
                puntos = [];
                marcadores.clear();
                resaltarTarjeta(null);
                desactivarBotones('No se pudieron cargar los puntos del mapa');
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
        return { mapa, cargarCentros, ajustarVista, seleccionarCentro };
    }

    if (typeof module !== 'undefined' && module.exports) {
        module.exports = { esUbicable, iniciarMapa, crearFicha, identificadorCentro };
    } else {
        global.EcoUVGMapa = { esUbicable, iniciarMapa, crearFicha, identificadorCentro };
        if (global.document.readyState === 'loading') {
            global.document.addEventListener('DOMContentLoaded', () => iniciarMapa());
        } else {
            iniciarMapa();
        }
    }
})(globalThis);
