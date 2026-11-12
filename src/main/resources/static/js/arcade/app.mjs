import {EcoRun,EcoSnake,EcoBreakout} from './modelo.mjs';
import {Dibujante,PALETA} from './vista.mjs';
const CONFIG={
    'eco-run':{clase:EcoRun,nombre:'Eco Run',instrucciones:'Flechas o WASD para moverte. Eco recoge un residuo a la vez: revisa su dibujo y nombre arriba del tablero y llévalo al contenedor de su material. Los números del tablero corresponden a la leyenda. Evita a los monstruos; cada entrega correcta los transforma en árboles durante 3 segundos. Completa 4 niveles.'},
    'eco-snake':{clase:EcoSnake,nombre:'Eco Snake',instrucciones:'Flechas o WASD para dirigir a Eco. Recoge solo el material solicitado. Su dibujo y color aparecen arriba y los residuos correctos tienen borde blanco. La fila toma el color del último acierto; un error cuesta una vida. Tu fila crece con cada acierto. Evita los bordes y tu propia fila. El material cambia cada 3 aciertos: consigue 12 para ganar.'},
    'eco-breakout':{clase:EcoBreakout,nombre:'Eco Breakout',instrucciones:'Mueve a Eco con ← →, A/D, el ratón o arrastrando el dedo. Rebota la pelota con su plataforma. Hay 50 bloques cuadrados. Rompe el material iluminado: los otros quedan transparentes y la pelota los atraviesa. Cambia de ángulo con los bordes de la plataforma. Si pasan 12 segundos sin romper un bloque, Eco relanza sin quitar vidas. Vacía el tablero para recuperar el bosque.'}
};
const TECLAS={ArrowUp:'arriba',ArrowDown:'abajo',ArrowLeft:'izquierda',ArrowRight:'derecha',w:'arriba',s:'abajo',a:'izquierda',d:'derecha'};
class Records {
    leer(id) { try { const n=Number(sessionStorage.getItem(`ecouvg.arcade.${id}`) ?? (id==='eco-run' ? sessionStorage.getItem('ecouvg.arcade.rec-man') : null)); return Number.isFinite(n)&&n>0 ? n : 0; } catch { return 0; } }
    guardar(id,puntos) { try { sessionStorage.setItem(`ecouvg.arcade.${id}`,String(Math.max(puntos,this.leer(id)))); } catch { /* Jugar sigue disponible sin almacenamiento. */ } }
}
class EcoArcade {
    constructor() {
        this.raiz=document.querySelector('#arcade'); this.canvas=document.querySelector('#tablero');
        this.vista=new Dibujante(this.canvas); this.records=new Records(); this.juego=null; this.id=null;
        this.materiales=[]; this.ultimo=0; this.estadoAnterior=null; this.actualizarRecords();
        this.conectar(); this.cargar(); requestAnimationFrame(t=>this.fotograma(t));
    }
    async cargar() {
        const carga=document.querySelector('#carga'), reintentar=document.querySelector('#reintentar');
        reintentar.hidden=true; carga.hidden=false; carga.textContent='Cargando materiales de la guía…';
        const controlador=new AbortController(), temporizador=setTimeout(()=>controlador.abort(),10000);
        try {
            const respuesta=await fetch(this.raiz.dataset.materialesUrl,{headers:{Accept:'application/json'},signal:controlador.signal});
            if(respuesta.redirected || respuesta.status===401 || respuesta.status===403) throw new Error('Tu sesión terminó. Vuelve a Inicio e inicia sesión para jugar.');
            if(!respuesta.ok) throw new Error('No se pudieron cargar los materiales. Comprueba que la guía tenga Plásticos, Metales, Papel y Cartón con ejemplos.');
            const datos=await respuesta.json();
            if(!Array.isArray(datos)||datos.length!==4||datos.some(m=>typeof m.nombre!=='string'||!Array.isArray(m.ejemplos)||!m.ejemplos.length)) throw new Error('La guía no tiene los materiales necesarios para jugar.');
            this.materiales=datos; carga.hidden=true;
            document.querySelectorAll('[data-juego]').forEach(b=>b.disabled=false);
            document.querySelectorAll('[data-preview]').forEach(c=>new Dibujante(c).dibujar(new CONFIG[c.dataset.preview].clase(datos)));
            const leyenda=document.querySelector('#leyenda'); leyenda.replaceChildren();
            datos.forEach((m,i)=>{ const li=document.createElement('li'),n=document.createElement('span'),texto=document.createElement('span');
                n.className='material-numero';n.style.background=PALETA[i];n.textContent=i+1;texto.textContent=m.nombre; const icono=document.createElement('canvas');icono.width=40;icono.height=40;icono.setAttribute('aria-hidden','true');new Dibujante(icono).iconoResiduo(i,20,20,34);li.append(n,icono,texto);leyenda.append(li); });
        } catch(error) {
            carga.textContent=error.name==='AbortError'?'La conexión tardó demasiado. Vuelve a intentarlo.':error.message;
            reintentar.hidden=false;
        } finally { clearTimeout(temporizador); }
    }
    conectar() {
        document.querySelector('#reintentar').addEventListener('click',()=>this.cargar());
        document.querySelectorAll('[data-juego]').forEach(b=>b.addEventListener('click',()=>this.seleccionar(b.dataset.juego)));
        document.querySelector('#volver').addEventListener('click',()=>this.volver());
        document.querySelector('#reiniciar').addEventListener('click',()=>this.seleccionar(this.id));
        document.querySelector('#pausar').addEventListener('click',()=>this.pausa());
        document.querySelector('#accion').addEventListener('click',()=>{
            if(['fin','victoria'].includes(this.juego.partida.estado)) this.seleccionar(this.id);
            this.juego.iniciar(); this.juego.continuar(); this.canvas.focus({preventScroll:true}); this.sincronizar();
        });
        window.addEventListener('keydown',e=>{
            if(!this.juego || /INPUT|TEXTAREA|SELECT/.test(e.target.tagName)) return;
            const d=TECLAS[e.key]||TECLAS[e.key.toLowerCase()];
            if(d && this.juego.partida.estado==='jugando') {e.preventDefault();this.juego.dirigir(d);}
            if(e.key.toLowerCase()==='p' && !e.repeat) {e.preventDefault();this.pausa();}
        });
        window.addEventListener('keyup',e=>{
            if(this.juego instanceof EcoBreakout && ['ArrowLeft','ArrowRight','a','d','A','D'].includes(e.key)) this.juego.dirigir(null);
        });
        document.querySelectorAll('[data-direccion]').forEach(b=>{
            b.addEventListener('pointerdown',e=>{
                if(this.juego?.partida.estado!=='jugando') return;
                e.preventDefault();b.setPointerCapture(e.pointerId);this.juego.dirigir(b.dataset.direccion);
            });
            const soltar=()=>{if(this.juego instanceof EcoBreakout) this.juego.dirigir(null);};
            b.addEventListener('pointerup',soltar);b.addEventListener('pointercancel',soltar);b.addEventListener('lostpointercapture',soltar);
            // Activación por teclado/tecnología de asistencia.
            b.addEventListener('click',e=>{if(e.detail===0 && this.juego?.partida.estado==='jugando') {
                if(this.juego instanceof EcoBreakout) this.juego.apuntar(this.juego.tortuga.x+(b.dataset.direccion==='izquierda'?-45:45));
                else this.juego.dirigir(b.dataset.direccion);
            }});
        });
        const apuntar=e=>{
            if(!(this.juego instanceof EcoBreakout)||this.juego.partida.estado!=='jugando') return;
            const r=this.canvas.getBoundingClientRect();this.juego.apuntar((e.clientX-r.left)*640/r.width);
        };
        this.canvas.addEventListener('pointerdown',e=>{this.canvas.setPointerCapture(e.pointerId);apuntar(e);});
        this.canvas.addEventListener('pointermove',apuntar);
        document.addEventListener('visibilitychange',()=>{if(document.hidden) this.pausarAutomaticamente();});
        window.addEventListener('blur',()=>this.pausarAutomaticamente());
    }
    seleccionar(id) {
        if(this.juego) this.records.guardar(this.id,this.juego.partida.puntos);
        this.id=id;this.juego=new CONFIG[id].clase(this.materiales);this.estadoAnterior=null;
        document.querySelector('#salon').hidden=true;document.querySelector('#partida').hidden=false;
        document.querySelector('#nombre-juego').textContent=CONFIG[id].nombre;
        document.querySelector('#instrucciones').textContent=CONFIG[id].instrucciones;
        document.querySelector('.controles').classList.toggle('breakout',id==='eco-breakout');
        this.canvas.setAttribute('aria-label',`${CONFIG[id].nombre}. ${id==='eco-breakout'?'Usa izquierda y derecha.':'Usa las flechas o WASD.'} P para pausar.`);
        this.sincronizar();this.vista.dibujar(this.juego);
        document.querySelector('#accion').focus({preventScroll:true});document.querySelector('#partida').scrollIntoView({block:'start'});
    }
    volver() {
        this.records.guardar(this.id,this.juego.partida.puntos);this.juego=null;this.actualizarRecords();
        document.querySelector('#partida').hidden=true;document.querySelector('#salon').hidden=false;
        document.querySelector(`[data-juego="${this.id}"]`).focus();
    }
    pausa() {
        if(!this.juego) return;
        if(this.juego.partida.estado==='jugando') this.juego.pausar();else this.juego.continuar();
        if(this.juego instanceof EcoBreakout) this.juego.dirigir(null);
        this.sincronizar();
    }
    pausarAutomaticamente() { if(this.juego) {this.juego.pausar();if(this.juego instanceof EcoBreakout) this.juego.dirigir(null);this.sincronizar();} }
    actualizarRecords() {document.querySelectorAll('[data-record]').forEach(el=>el.textContent=`Récord de esta pestaña: ${this.records.leer(el.dataset.record)}`);}
    texto(id,valor) {const el=document.getElementById(id);if(el.textContent!==String(valor)) el.textContent=valor;}
    sincronizar() {
        if(!this.juego) return;
        const j=this.juego,p=j.partida;
        this.texto('puntos',p.puntos);this.texto('vidas',p.vidas);this.texto('nivel',p.nivel);
        this.texto('objetivo',j.objetivo);this.texto('mensaje',j.mensaje);this.actualizarMaterial(j);
        const residuos=j.residuos?.map(r=>`${r.material+1}: ${r.ejemplo}`) || [];
        this.texto('residuos-actuales',residuos.length ? `En el tablero: ${[...new Set(residuos)].join(' · ')}`:'');
        if(p.estado!==this.estadoAnterior) {
            this.estadoAnterior=p.estado;
            document.querySelector('#capa').hidden=p.estado==='jugando';
            const pausa=document.querySelector('#pausar');pausa.disabled=!['jugando','pausa'].includes(p.estado);pausa.textContent=p.estado==='pausa'?'Continuar':'Pausar';
            const textos={lista:['ECO ESTÁ LISTA','¿Comenzamos?','Lee tu misión y los materiales antes de jugar. Usa P para pausar.','Jugar'],pausa:['RESPIRA UN MOMENTO','Partida en pausa','Tus puntos y vidas siguen aquí.','Continuar'],fin:['UNA NUEVA OPORTUNIDAD','¡Inténtalo otra vez!',`Conseguiste ${p.puntos} puntos. Cada partida te ayuda a reconocer mejor los materiales.`,'Volver a jugar'],victoria:['MISIÓN CUMPLIDA','¡Bien hecho, Eco!',`${p.puntos} puntos y un rincón más verde. Lleva lo aprendido a tu próximo reciclaje.`,'Volver a jugar']};
            if(textos[p.estado]) ['estado','titulo-capa','texto-capa','accion'].forEach((id,i)=>this.texto(id,textos[p.estado][i]));
            if(['fin','victoria'].includes(p.estado)) {this.records.guardar(this.id,p.puntos);document.querySelector('#accion').focus({preventScroll:true});}
        }
    }
    actualizarMaterial(j) {
        const material=j instanceof EcoRun ? j.carga?.material : j.material;
        const panel=document.querySelector('#objetivo-panel');
        const icono=document.querySelector('#icono-objetivo');
        const clave=material??'ninguno';
        if(icono.dataset.material!==String(clave)) {
            icono.dataset.material=clave;icono.getContext('2d').clearRect(0,0,72,72);
            if(material!==undefined && material!==null) new Dibujante(icono).iconoResiduo(material,36,36,58);
        }
        icono.hidden=material===undefined||material===null;
        panel.style.setProperty('--color-material',PALETA[material]||'#b7d5a0');
        this.texto('accion-objetivo',j instanceof EcoRun ? (j.carga?'ESTÁS LLEVANDO':'BUSCA UN RESIDUO') : j instanceof EcoSnake?'RECOGE SOLO ESTE MATERIAL':'ROMPE LOS BLOQUES ILUMINADOS');
        document.querySelectorAll('#leyenda li').forEach((li,i)=>li.classList.toggle('material-activo',i===material));
        const ultimo=j.ultimoResiduo,recogida=document.querySelector('#recogida-panel');
        recogida.hidden=!ultimo;
        if(ultimo) {
            recogida.style.setProperty('--color-material',PALETA[ultimo.material]);
            recogida.classList.toggle('incorrecto',ultimo.correcto===false);
            const canvas=document.querySelector('#icono-recogido');
            if(canvas.dataset.material!==String(ultimo.material)) {
                canvas.dataset.material=ultimo.material;canvas.getContext('2d').clearRect(0,0,48,48);
                new Dibujante(canvas).iconoResiduo(ultimo.material,24,24,38);
            }
            this.texto('ultimo-residuo',`${j instanceof EcoBreakout?'Último bloque':'Último residuo'}: ${ultimo.ejemplo} · ${this.materiales[ultimo.material].nombre}${ultimo.correcto===false?' — No era el material solicitado.':ultimo.correcto?' — ¡Correcto!':''}`);
        }
    }
    fotograma(t) {
        const dt=Math.min((t-this.ultimo)/1000 || 0,.05);this.ultimo=t;
        if(this.juego) {this.juego.actualizar(dt);this.vista.dibujar(this.juego);this.sincronizar();}
        requestAnimationFrame(siguiente=>this.fotograma(siguiente));
    }
}
new EcoArcade();
