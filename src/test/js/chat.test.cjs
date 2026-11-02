const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

class Elemento {
    constructor(etiqueta = 'div') {
        this.tagName = etiqueta.toUpperCase(); this.hidden = false;
        this.children = []; this.handlers = {}; this.dataset = {}; this.style = {}; this.value = '';
        this.className = ''; this.disabled = false; this.scrollHeight = 60;
        this.classList = {add: (...clases) => { this.className += ' ' + clases.join(' '); }};
    }
    set textContent(texto) { this.texto = texto; this.children = []; }
    get textContent() { return (this.texto || '') + this.children.map(hijo => hijo.textContent).join(''); }
    appendChild(hijo) { this.children.push(hijo); hijo.parent = this; return hijo; }
    append(...hijos) { hijos.forEach(hijo => this.appendChild(hijo)); }
    addEventListener(tipo, fn) { this.handlers[tipo] = fn; }
    setAttribute(nombre, valor) { this[nombre] = valor; }
    removeAttribute(nombre) { delete this[nombre]; }
    prepend(hijo) { this.children.unshift(hijo); hijo.parent = this; }
    focus() { this.focused = true; }
    remove() { this.parent.children = this.parent.children.filter(hijo => hijo !== this); }
    querySelector(selector) {
        if (selector.startsWith('[data-chat-id=')) {
            const id = selector.slice(15, -2);
            return this.children.find(hijo => hijo.dataset.chatId === id) || null;
        }
        const ultimo = selector.split(' ').at(-1);
        const clase = ultimo.replace('.', '');
        for (const hijo of this.children) {
            if (ultimo.startsWith('.') ? hijo.className.split(' ').includes(clase) : hijo.tagName === ultimo.toUpperCase()) return hijo;
            const encontrado = hijo.querySelector(selector);
            if (encontrado) return encontrado;
        }
        return null;
    }
    querySelectorAll() { return []; }
}

module.exports = async function probarChat() {
    const ids = Object.fromEntries(['form-chat', 'pregunta', 'mensajes', 'btn-enviar', 'estado-chat', 'sugerencias', 'contador-chat', 'lista-conversaciones'].map(id => [id, new Elemento()]));
    ids['form-chat'].dataset.url = '/app/chat/preguntar';
    ids['form-chat'].dataset.crearUrl = '/app/chat/conversaciones';
    ids['form-chat'].dataset.conversacionId = '11111111-1111-4111-8111-111111111111';
    ids['lista-conversaciones'].dataset.chatUrl = '/app/chat';
    const navegaciones = [];
    for (const tipo of ['eco', 'usuario']) {
        ids['avatar-' + tipo] = {content: {cloneNode() {
            const avatar = new Elemento(); avatar.className = 'avatar-' + tipo;
            if (tipo === 'usuario') {
                const foto = new Elemento('img');
                // Una imagen dentro de un template puede figurar completa sin haber cargado.
                foto.complete = true; foto.naturalWidth = 0;
                avatar.appendChild(foto);
            }
            return avatar;
        }}};
    }
    const peticiones = [];
    let resolver;
    const contexto = vm.createContext({
        document: {getElementById: id => ids[id], createElement: etiqueta => new Elemento(etiqueta), createTextNode: texto => { const nodo = new Elemento('#text'); nodo.textContent = texto; return nodo; }, querySelector: () => ({content: 'csrf-test'}), querySelectorAll: () => []},
        fetch: (url, opciones) => { peticiones.push({url, opciones}); return new Promise(resolve => { resolver = resolve; }); },
        AbortController, setTimeout, clearTimeout, Date, URL,
        crypto: require('node:crypto').webcrypto,
        window: {location: {origin: 'https://ecouvg.test'}, history: {replaceState: (...args) => navegaciones.push(args)}}
    });
    vm.runInContext(fs.readFileSync(path.resolve(__dirname, '../../main/resources/static/js/chat.js'), 'utf8'), contexto);
    const enviar = (texto, fila) => { contexto.textoPrueba = texto; contexto.filaPrueba = fila; return vm.runInContext('enviar(textoPrueba, filaPrueba)', contexto); };
    await enviar('  ');
    await enviar('x'.repeat(1001));
    assert.equal(peticiones.length, 0);
    const primera = enviar('<script>texto</script>');
    assert.equal(ids['btn-enviar'].disabled, true);
    await enviar('duplicada');
    await new Promise(resolve => setImmediate(resolve));
    assert.equal(peticiones.length, 1);
    assert.equal(peticiones[0].url, '/app/chat/preguntar');
    assert.equal(JSON.parse(peticiones[0].opciones.body).pregunta, '<script>texto</script>');
    assert.equal(ids.mensajes.children.length, 2);
    assert.equal(ids.mensajes.children[0].children[0].className, 'avatar-usuario');
    assert.equal(ids.mensajes.children[1].children[0].className, 'avatar-eco');
    assert.equal(ids.mensajes.children[0].querySelector('.burbuja-chat').textContent, '<script>texto</script>');
    const foto = ids.mensajes.children[0].children[0].querySelector('img');
    assert.equal(foto.hidden, false, 'No debe ocultar la foto antes de cargar');
    foto.handlers.load();
    assert.equal(foto.hidden, false);
    foto.handlers.error();
    assert.equal(foto.hidden, true);
    foto.handlers.load();
    assert.equal(foto.hidden, false, 'Una carga exitosa debe volver a mostrar la foto');
    resolver({ok: true, json: async () => ({respuesta: '**Respuesta** de Eco'})});
    await primera;
    assert.equal(ids.mensajes.children[1].querySelector('.burbuja-chat').textContent, 'Respuesta de Eco');
    assert.equal(ids.mensajes.children[1].querySelector('strong').textContent, 'Respuesta');
    assert.equal(ids['btn-enviar'].disabled, false);
    const segunda = enviar('otra pregunta');
    await new Promise(resolve => setImmediate(resolve));
    resolver({ok: false});
    await segunda;
    const fila = ids.mensajes.children[3].children[1];
    assert.ok(fila.querySelector('.reintentar-chat'));
    const reintento = enviar('otra pregunta', fila);
    await new Promise(resolve => setImmediate(resolve));
    assert.equal(ids.mensajes.children.length, 4);
    resolver({ok: true, json: async () => ({respuesta: 'Ahora sí'})});
    await reintento;
    assert.equal(fila.querySelector('.burbuja-chat').textContent, 'Ahora sí');
    assert.equal(fila.querySelector('.reintentar-chat'), null);
    assert.equal(ids['btn-enviar'].disabled, false);
    assert.equal(JSON.parse(peticiones[1].opciones.body).solicitudId, JSON.parse(peticiones[2].opciones.body).solicitudId);
    assert.equal(JSON.parse(peticiones[0].opciones.body).conversacionId, ids['form-chat'].dataset.conversacionId);
    vm.runInContext('conversacionId = null', contexto);
    const nuevo = enviar('Conversación nueva');
    assert.equal(peticiones[3].url, '/app/chat/conversaciones');
    const nuevoId = '22222222-2222-4222-8222-222222222222';
    resolver({ok: true, json: async () => ({id: nuevoId})});
    await new Promise(resolve => setImmediate(resolve));
    assert.equal(JSON.parse(peticiones[4].opciones.body).conversacionId, nuevoId);
    assert.ok(String(navegaciones[0][2]).includes('conversacion=' + nuevoId));
    resolver({ok: true, json: async () => ({respuesta: 'Guardada'})});
    await nuevo;
    assert.equal(ids['estado-chat'].textContent, 'Guardado');
    assert.equal(ids['lista-conversaciones'].children[0].textContent, 'Conversación nueva');
    const destino = new Elemento();
    contexto.destinoPrueba = destino;
    const formatear = texto => { contexto.formatoPrueba = texto; vm.runInContext('formatearRespuesta(destinoPrueba, formatoPrueba)', contexto); };
    formatear('**Negrita** y ***ambas*** y *cursiva* y `**literal**`');
    assert.equal(destino.textContent, 'Negrita y ambas y cursiva y **literal**');
    assert.equal(destino.querySelector('strong').textContent, 'Negrita');
    assert.equal(destino.querySelector('em').textContent, 'ambas');
    assert.equal(destino.querySelector('code').textContent, '**literal**');
    formatear('* **Papel:** limpio\n* **Vidrio:** separado');
    assert.equal(destino.textContent, '* Papel: limpio\n* Vidrio: separado');
    formatear('**<img src=x onerror=alert(1)>** <script>alert(1)</script>');
    assert.equal(destino.querySelector('img'), null);
    assert.equal(destino.querySelector('script'), null);
    assert.equal(destino.querySelector('strong').textContent, '<img src=x onerror=alert(1)>');
    formatear('Un asterisco * aislado');
    assert.equal(destino.textContent, 'Un asterisco * aislado');
    return 'Chat verificado: formato negrita/cursiva, foto antes y después de cargar y fallback; avatares, texto seguro, envío único, validación, recuperación y reintento sin duplicados.';
};
if (require.main === module) module.exports().then(console.log).catch(error => { console.error(error); process.exitCode = 1; });
