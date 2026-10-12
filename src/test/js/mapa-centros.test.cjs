const test = require('node:test');
const assert = require('node:assert/strict');
const { esUbicable, iniciarMapa } = require('../../main/resources/static/js/mapa-centros.js');

const centro = (extra = {}) => ({ id: 1, nombre: 'Cayalá', latitud: 14.6, longitud: -90.4, ubicable: true, ...extra });
function respuesta(datos, extra = {}) {
    return { ok: true, status: 200, redirected: false,
        headers: { get: () => 'application/json;charset=UTF-8' },
        json: async () => datos, ...extra };
}
function preparar(responder = async () => respuesta([centro()])) {
    const elementos = {};
    for (const id of ['mapa-centros', 'mapa-estado', 'mapa-aviso', 'mapa-encuadrar', 'mapa-reintentar', 'mapa-login']) {
        elementos[id] = { dataset: {}, hidden: true, disabled: false, textContent: '', atributos: {},
            setAttribute(nombre, valor) { this.atributos[nombre] = valor; } };
    }
    elementos['mapa-centros'].dataset = { apiUrl: '/eco/api/centros/mapa?q=cayala&material=Cart%C3%B3n', tilesUrl: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png' };
    const llamadas = { vistas: [], limites: [], consultas: [], recargas: 0, mapas: 0 };
    const mapa = { setView(pos, zoom) { llamadas.vistas.push([pos, zoom]); return this; },
        fitBounds(bounds, options) { llamadas.limites.push(options); }, invalidateSize() {} };
    const grupo = { markers: [], addTo() { return this; }, clearLayers() { this.markers = []; }, getBounds() { return 'bounds'; } };
    const capa = { eventos: {}, on(evento, fn) { this.eventos[evento] = fn; return this; }, addTo() { return this; } };
    const entorno = {
        document: { getElementById: id => elementos[id], createElement: tag => ({ tag, textContent: '' }) },
        location: { reload() { llamadas.recargas++; } },
        AbortController, setTimeout, clearTimeout,
        fetch: async (...args) => { llamadas.consultas.push(args); return responder(...args); },
        L: {
            map() { llamadas.mapas++; return mapa; },
            tileLayer(url, options) { llamadas.tilesUrl = url; llamadas.tileOptions = options; return capa; },
            featureGroup() { return grupo; },
            marker(pos, options) { return { pos, options, bindTooltip(el) { this.etiqueta = el; return this; }, addTo(g) { g.markers.push(this); return this; } }; }
        }
    };
    return { elementos, llamadas, mapa, grupo, capa, entorno };
}

test('acepta coordenadas finitas y límites; rechaza nulos, strings, NaN e infinito', () => {
    for (const c of [centro(), centro({latitud: 0,longitud: 0}), centro({latitud: 90,longitud: 180}), centro({latitud: -90,longitud: -180})]) assert.equal(esUbicable(c), true);
    for (const c of [null, {}, centro({ubicable:false}), centro({latitud:null}), centro({longitud:'-90.4'}), centro({latitud:NaN}), centro({longitud:Infinity}), centro({latitud:91}), centro({longitud:-181})]) assert.equal(esUbicable(c), false);
});
test('un centro produce un marcador y zoom acotado', async () => {
    const h=preparar(); await iniciarMapa(h.entorno);
    assert.equal(h.grupo.markers.length,1); assert.deepEqual(h.llamadas.vistas.at(-1),[[14.6,-90.4],15]);
    assert.equal(h.elementos['mapa-encuadrar'].disabled,false);
    assert.equal(h.elementos['mapa-centros'].atributos['aria-busy'],'false');
});
test('varios centros se encuadran con margen y límite de zoom', async () => {
    const h=preparar(async()=>respuesta([centro(),centro({id:2,latitud:14.7})])); await iniciarMapa(h.entorno);
    assert.equal(h.grupo.markers.length,2); assert.equal(h.llamadas.limites[0].maxZoom,16);
    h.elementos['mapa-encuadrar'].onclick(); assert.equal(h.llamadas.limites.length,2);
});
test('mantiene consulta del servidor y envía la sesión en la petición', async () => {
    const h=preparar(); await iniciarMapa(h.entorno);
    assert.equal(h.llamadas.consultas[0][0],h.elementos['mapa-centros'].dataset.apiUrl);
    assert.equal(h.llamadas.consultas[0][1].credentials,'same-origin');
});
test('omite marcadores inválidos y explica cuántos no tienen ubicación', async () => {
    const h=preparar(async()=>respuesta([centro(),centro({latitud:null})])); await iniciarMapa(h.entorno);
    assert.equal(h.grupo.markers.length,1); assert.match(h.elementos['mapa-estado'].textContent,/Sin ubicación válida: 1/);
});
test('lista vacía no intenta encuadrar puntos inexistentes', async () => {
    const h=preparar(async()=>respuesta([])); await iniciarMapa(h.entorno);
    assert.equal(h.grupo.markers.length,0);assert.equal(h.llamadas.limites.length,0);
    assert.match(h.elementos['mapa-estado'].textContent,/No hay centros/);assert.equal(h.elementos['mapa-encuadrar'].disabled,true);
});
test('todos sin coordenadas conservan la vista general', async () => {
    const h=preparar(async()=>respuesta([centro({ubicable:false})]));await iniciarMapa(h.entorno);
    assert.match(h.elementos['mapa-estado'].textContent,/aún no tienen coordenadas/);assert.equal(h.llamadas.vistas.length,1);
});
test('el nombre se trata como texto en lugar de HTML ejecutable', async () => {
    const nombre='<img src=x onerror=alert(1)>';
    const h=preparar(async()=>respuesta([centro({nombre})])); await iniciarMapa(h.entorno);
    assert.equal(h.grupo.markers[0].etiqueta.textContent,nombre);
    assert.equal(h.grupo.markers[0].etiqueta.innerHTML,undefined);
});
test('sesión expirada ofrece enlace de inicio de sesión', async () => {
    for (const extra of [{redirected:true},{status:401,ok:false},{status:403,ok:false}]) {
        const h=preparar(async()=>respuesta([],extra));await iniciarMapa(h.entorno);
        assert.equal(h.elementos['mapa-login'].hidden,false);assert.equal(h.grupo.markers.length,0);
    }
});
test('rechaza HTML y formatos inesperados sin crear marcadores', async () => {
    for (const res of [respuesta([],{headers:{get:()=> 'text/html'}}),respuesta({centros:[]})]) {
        const h=preparar(async()=>res);await iniciarMapa(h.entorno);
        assert.equal(h.grupo.markers.length,0);assert.equal(h.elementos['mapa-reintentar'].hidden,false);
    }
});
test('fallo HTTP permite reintentar sin duplicar mapas ni marcadores', async () => {
    let veces=0;const h=preparar(async()=>++veces===1?respuesta([],{ok:false,status:500}):respuesta([centro()]));
    const app=await iniciarMapa(h.entorno);assert.equal(h.elementos['mapa-reintentar'].hidden,false);
    await app.cargarCentros();await app.cargarCentros();
    assert.equal(h.grupo.markers.length,1);assert.equal(h.llamadas.mapas,1);
});
test('fallo de red y timeout muestran mensajes recuperables', async () => {
    for (const error of [new TypeError('network'),Object.assign(new Error('timeout'),{name:'AbortError'})]) {
        const h=preparar(async()=>{throw error;});await iniciarMapa(h.entorno);
        assert.equal(h.elementos['mapa-reintentar'].hidden,false);
        assert.equal(h.elementos['mapa-centros'].atributos['aria-busy'],'false');
    }
});
test('aviso de teselas permanece si hubo fallos; se oculta tras una carga sin fallos', async () => {
    const h=preparar();await iniciarMapa(h.entorno);
    h.capa.eventos.loading();h.capa.eventos.tileerror();h.capa.eventos.load();assert.equal(h.elementos['mapa-aviso'].hidden,false);
    h.capa.eventos.loading();h.capa.eventos.load();assert.equal(h.elementos['mapa-aviso'].hidden,true);
    assert.match(h.llamadas.tileOptions.attribution,/OpenStreetMap/);
    assert.equal(h.llamadas.tilesUrl,'https://tile.openstreetmap.org/{z}/{x}/{y}.png');
});
test('Leaflet ausente muestra un aviso y permite recargar', async () => {
    const h=preparar();delete h.entorno.L;await iniciarMapa(h.entorno);
    assert.equal(h.llamadas.consultas.length,0);assert.match(h.elementos['mapa-estado'].textContent,/No se pudo cargar el mapa/);
    h.elementos['mapa-reintentar'].onclick();assert.equal(h.llamadas.recargas,1);
});
test('no inicializa dos veces el mismo contenedor', async () => {
    const h=preparar();await iniciarMapa(h.entorno);await iniciarMapa(h.entorno);
    assert.equal(h.llamadas.mapas,1);assert.equal(h.llamadas.consultas.length,1);
});
