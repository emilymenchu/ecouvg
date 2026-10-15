const test = require('node:test');
const assert = require('node:assert/strict');
const { esUbicable, iniciarMapa, crearFicha, identificadorCentro } = require('../../main/resources/static/js/mapa-centros.js');

const centro = (extra = {}) => ({ id: 1, nombre: 'Cayalá', latitud: 14.6, longitud: -90.4, ubicable: true, ...extra });
function respuesta(datos, extra = {}) {
    return { ok: true, status: 200, redirected: false,
        headers: { get: () => 'application/json;charset=UTF-8' },
        json: async () => datos, ...extra };
}
function nodo(tag = 'div') {
    const clases = new Set();
    return { tag, children: [], dataset: {}, atributos: {}, textContent: '',
        appendChild(n) { this.children.push(n); return n; },
        setAttribute(k,v) { this.atributos[k]=v; },
        classList: { toggle(k,activo) { if(activo)clases.add(k);else clases.delete(k); }, contains(k) { return clases.has(k); } },
        scrollIntoView(opciones) { this.scroll=opciones; }, focus() { this.enfocado=true; } };
}
function preparar(responder = async () => respuesta([centro()])) {
    const elementos = {};
    for (const id of ['mapa-centros', 'mapa-estado', 'mapa-aviso', 'mapa-encuadrar', 'mapa-reintentar', 'mapa-login']) {
        elementos[id] = { ...nodo(), dataset: {}, hidden: true, disabled: false, textContent: '', atributos: {},
            setAttribute(nombre, valor) { this.atributos[nombre] = valor; } };
    }
    elementos['mapa-centros'].dataset = { apiUrl: '/eco/api/centros/mapa?q=cayala&material=Cart%C3%B3n', tilesUrl: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png' };
    const tarjetas = [1,2].map(id => Object.assign(nodo(), {dataset:{centroTarjeta:String(id)}}));
    const botones = [1,2].map(id => Object.assign(nodo('button'), {dataset:{verCentro:String(id)}}));
    const llamadas = { vistas: [], limites: [], consultas: [], recargas: 0, mapas: 0 };
    const mapa = { setView(pos, zoom) { llamadas.vistas.push([pos, zoom]); return this; },
        closePopup() { for(const m of grupo.markers) m.closePopup(); },
        fitBounds(bounds, options) { llamadas.limites.push(options); }, invalidateSize() {} };
    const grupo = { markers: [], addTo() { return this; }, clearLayers() { this.markers = []; }, getBounds() { return 'bounds'; } };
    const capa = { eventos: {}, on(evento, fn) { this.eventos[evento] = fn; return this; }, addTo() { return this; } };
    const entorno = {
        document: { getElementById: id => elementos[id], createElement: nodo, querySelectorAll: selector => selector === '[data-ver-centro]' ? botones : tarjetas },
        location: { reload() { llamadas.recargas++; } },
        AbortController, setTimeout, clearTimeout,
        fetch: async (...args) => { llamadas.consultas.push(args); return responder(...args); },
        L: {
            map() { llamadas.mapas++; return mapa; },
            tileLayer(url, options) { llamadas.tilesUrl = url; llamadas.tileOptions = options; return capa; },
            featureGroup() { return grupo; },
            marker(pos, options) { return { pos, options, eventos: {}, abierto:false,
                bindTooltip(el) { this.etiqueta=el;return this; },
                bindPopup(el,opciones) { this.ficha=el;this.popupOptions=opciones;return this; },
                on(evento,fn) { this.eventos[evento]=fn;return this; },
                getLatLng() {return this.pos;},
                openPopup() {this.abierto=true;this.eventos.popupopen?.();return this;},
                closePopup() {if(this.abierto){this.abierto=false;this.eventos.popupclose?.();}return this;},
                addTo(g) {g.markers.push(this);return this;}
            }; }
        }
    };
    return { elementos, llamadas, mapa, grupo, capa, entorno, tarjetas, botones };
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

test('la ventana contiene datos completos y enlace relativo al contexto de la aplicación', () => {
    const ficha=crearFicha({createElement:nodo},centro({ubicacion:'Zona 16',horario:'Lunes',materiales:['Cartón','Vidrio']}),'/eco/centros/',null);
    assert.match(ficha.children.map(n=>n.textContent).join(' '),/Zona 16.*Horario: Lunes.*Cartón, Vidrio/);
    assert.equal(ficha.children.find(n=>n.tag==='a').href,'/eco/centros/1');
});
test('no crea enlaces a partir de IDs inválidos y muestra valores pendientes', () => {
    for(const id of [null,-1,0,'javascript:alert(1)',1.2,Number.MAX_SAFE_INTEGER+1]) assert.equal(identificadorCentro(id),null);
    const ficha=crearFicha({createElement:nodo},{id:null},'/centros/',null);
    assert.equal(ficha.children.some(n=>n.tag==='a'),false);
    assert.match(ficha.children.map(n=>n.textContent).join(' '),/Dirección por confirmar.*Por confirmar.*Materiales por confirmar/);
});
test('los campos de la ventana se insertan como texto sin HTML', () => {
    const ataque='<img src=x onerror=alert(1)>';
    const ficha=crearFicha({createElement:nodo},centro({nombre:ataque,ubicacion:ataque,horario:ataque,materiales:[ataque]}),'/centros/',null);
    for(const hijo of ficha.children){assert.equal(hijo.innerHTML,undefined);assert.equal(hijo.children.length,0);}
    assert.equal(ficha.children[0].textContent,ataque);
});
test('Ver en mapa centra el marcador y abre su información', async () => {
    const h=preparar();await iniciarMapa(h.entorno);
    assert.equal(h.botones[0].disabled,false);h.botones[0].onclick();
    assert.equal(h.grupo.markers[0].abierto,true);assert.equal(h.llamadas.vistas.at(-1)[1],16);
    assert.equal(h.tarjetas[0].classList.contains('tarjeta-seleccionada'),true);
    assert.equal(h.botones[0].atributos['aria-pressed'],'true');assert.equal(h.elementos['mapa-centros'].enfocado,true);
});
test('no habilita Ver en mapa cuando el centro carece de coordenadas', async () => {
    const h=preparar(async()=>respuesta([centro({ubicable:false})]));const app=await iniciarMapa(h.entorno);
    assert.equal(h.botones[0].disabled,true);assert.equal(app.seleccionarCentro(1),false);
});
test('al abrir y cerrar un marcador se actualiza el resaltado de su tarjeta', async () => {
    const h=preparar();await iniciarMapa(h.entorno);h.grupo.markers[0].openPopup();
    assert.equal(h.tarjetas[0].classList.contains('tarjeta-seleccionada'),true);
    h.grupo.markers[0].closePopup();assert.equal(h.tarjetas[0].classList.contains('tarjeta-seleccionada'),false);
});
test('Ver tarjeta cierra la ventana y lleva el foco a la tarjeta correcta', async () => {
    const h=preparar();await iniciarMapa(h.entorno);const m=h.grupo.markers[0];m.openPopup();
    m.ficha.children.find(n=>n.tag==='button').onclick();
    assert.equal(m.abierto,false);assert.equal(h.tarjetas[0].enfocado,true);
    assert.equal(h.tarjetas[0].classList.contains('tarjeta-seleccionada'),true);
});
test('reintentar vacía selección y referencias a los marcadores anteriores', async () => {
    let vez=0;const h=preparar(async()=>respuesta(++vez===1?[centro()]:[]));
    const app=await iniciarMapa(h.entorno);h.botones[0].onclick();await app.cargarCentros();
    assert.equal(app.seleccionarCentro(1),false);assert.equal(h.botones[0].disabled,true);
    assert.equal(h.tarjetas[0].classList.contains('tarjeta-seleccionada'),false);
});
test('respeta la preferencia de movimiento reducido', async () => {
    const h=preparar();h.entorno.matchMedia=()=>({matches:true});await iniciarMapa(h.entorno);h.botones[0].onclick();
    assert.equal(h.elementos['mapa-centros'].scroll.behavior,'auto');
});

function prepararDetalle(responder = async () => respuesta(centro())) {
    const h = preparar(responder);
    Object.assign(h.elementos['mapa-centros'].dataset, {
        modo: 'detalle', centroId: '1', apiUrl: '/eco/api/centros/mapa/1'
    });
    h.entorno.document.querySelectorAll = () => [];
    return h;
}
test('detalle consulta un único centro y muestra su ubicación sin enlace a sí mismo', async () => {
    const h = prepararDetalle(); await iniciarMapa(h.entorno);
    assert.equal(h.llamadas.consultas[0][0], '/eco/api/centros/mapa/1');
    assert.equal(h.grupo.markers.length, 1);
    assert.equal(h.elementos['mapa-centros'].hidden, false);
    assert.deepEqual(h.llamadas.vistas.at(-1), [[14.6, -90.4], 15]);
    assert.equal(h.grupo.markers[0].ficha.children.some(n => n.tag === 'a' || n.tag === 'button'), false);
});
test('detalle sin coordenadas oculta el mapa y conserva una explicación', async () => {
    const h = prepararDetalle(async () => respuesta(centro({ubicable: false, latitud: null})));
    await iniciarMapa(h.entorno);
    assert.equal(h.elementos['mapa-centros'].hidden, true);
    assert.equal(h.grupo.markers.length, 0);
    assert.match(h.elementos['mapa-estado'].textContent, /dirección indicada arriba/);
    assert.equal(h.elementos['mapa-encuadrar'].disabled, true);
});
test('detalle rechaza otro centro y contratos JSON incorrectos', async () => {
    for (const datos of [centro({id: 2}), [centro()], null, {}]) {
        const h = prepararDetalle(async () => respuesta(datos)); await iniciarMapa(h.entorno);
        assert.equal(h.grupo.markers.length, 0);
        assert.equal(h.elementos['mapa-centros'].hidden, true);
        assert.equal(h.elementos['mapa-reintentar'].hidden, false);
    }
});
test('detalle inexistente informa que se debe volver al directorio', async () => {
    const h = prepararDetalle(async () => respuesta(null, {ok: false, status: 404}));
    await iniciarMapa(h.entorno);
    assert.match(h.elementos['mapa-estado'].textContent, /ya no está disponible/);
    assert.equal(h.elementos['mapa-centros'].hidden, true);
});
test('detalle se recupera de un error de red sin crear otro mapa', async () => {
    let vez = 0;
    const h = prepararDetalle(async () => { if (++vez === 1) throw new TypeError('network'); return respuesta(centro()); });
    const app = await iniciarMapa(h.entorno);
    assert.equal(h.elementos['mapa-centros'].hidden, true);
    await app.cargarCentros();
    assert.equal(h.elementos['mapa-centros'].hidden, false);
    assert.equal(h.llamadas.mapas, 1);
    assert.equal(h.grupo.markers.length, 1);
});
test('detalle con sesión caducada mantiene visible el enlace de acceso', async () => {
    const h = prepararDetalle(async () => respuesta(null, {redirected: true}));
    await iniciarMapa(h.entorno);
    assert.equal(h.elementos['mapa-login'].hidden, false);
    assert.equal(h.elementos['mapa-centros'].hidden, true);
});
