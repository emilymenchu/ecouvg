/** Lógica de juegos independiente del DOM: se puede probar con node --test. */
export const DIRECCIONES = Object.freeze({arriba:[0,-1], abajo:[0,1], izquierda:[-1,0], derecha:[1,0]});
export const mismo = (a,b) => a.x === b.x && a.y === b.y;
export const limitar = (n,min,max) => Math.max(min,Math.min(max,n));
export class Entidad {
    constructor(x,y) { this.x=x; this.y=y; }
}
export class Tortuga extends Entidad {
    constructor(x,y) { super(x,y); this.direccion='derecha'; }
}
export class Residuo extends Entidad {
    constructor(x,y,material,ejemplo) { super(x,y); this.material=material; this.ejemplo=ejemplo; }
}
export class Monstruo extends Entidad {
    constructor(x,y) { super(x,y); this.arbol=0; }
}
export class Bloque extends Entidad {
    constructor(x,y,material) { super(x,y); this.material=material; this.ancho=48; this.alto=48; this.activo=true; }
}
export class Pelota extends Entidad {
    constructor(x,y) { super(x,y); this.vx=155; this.vy=-235; this.radio=8; }
}
export class Partida {
    constructor() { this.puntos=0; this.aciertos=0; this.vidas=3; this.nivel=1; this.estado='lista'; }
    sumar(puntos) { this.puntos=Math.max(0,this.puntos+puntos); }
}
export class Juego {
    constructor(materiales,aleatorio=Math.random) {
        if (!Array.isArray(materiales) || materiales.length!==4 || materiales.some(m=>!m.ejemplos?.length)) {
            throw new Error('Se requieren cuatro materiales con ejemplos.');
        }
        this.materiales=materiales; this.aleatorio=aleatorio; this.partida=new Partida();
        this.mensaje='Pulsa Jugar cuando estés listo.'; this.tiempo=0; this.ultimoResiduo=null;
    }
    elegir(lista) { return lista[Math.floor(this.aleatorio()*lista.length)]; }
    ejemplo(i) { return this.elegir(this.materiales[i].ejemplos); }
    iniciar() { if(this.partida.estado==='lista') this.partida.estado='jugando'; }
    pausar() { if(this.partida.estado==='jugando') this.partida.estado='pausa'; }
    continuar() { if(this.partida.estado==='pausa') this.partida.estado='jugando'; }
    perderVida(texto) {
        this.partida.vidas--; this.mensaje=texto;
        if(this.partida.vidas<=0) this.partida.estado='fin';
    }
    ganar() { this.partida.estado='victoria'; this.mensaje='¡Lo lograste! Eco recuperó este rincón del planeta.'; }
    actualizar(dt) { if(this.partida.estado==='jugando') { this.tiempo+=dt; this.avanzar(dt); } }
    avanzar() { throw new Error('Cada juego debe implementar avanzar.'); }
    dirigir() { }
    get objetivo() { return ''; }
}

export class EcoRun extends Juego {
    constructor(materiales,aleatorio) { super(materiales,aleatorio); this.crearNivel(); }
    esPared(x,y) {
        return x<=0 || x>=19 || y<=0 || y>=14 ||
            ([4,8,11].includes(y) && x>=3 && x<=16 && ![6,12].includes(x));
    }
    celdasLibres() {
        const celdas=[];
        for(let y=1;y<14;y++) for(let x=1;x<19;x++) if(!this.esPared(x,y)) celdas.push({x,y});
        return celdas;
    }
    crearNivel() {
        this.tortuga=new Tortuga(1,1); this.carga=null; this.paso=0; this.pasoMonstruo=0;
        this.inmune=1; this.direccion=null;
        this.contenedores=[1,6,12,18].map((x,material)=>({x,y:13,material}));
        this.monstruos=[new Monstruo(18,1),new Monstruo(18,10)];
        this.residuos=[];
        const libres=this.celdasLibres().filter(c=>c.y>1 && c.y<12 && !this.monstruos.some(m=>mismo(m,c)));
        for(let material=0;material<this.partida.nivel;material++) for(let i=0;i<2;i++) {
            const indice=Math.floor(this.aleatorio()*libres.length), c=libres.splice(indice,1)[0];
            this.residuos.push(new Residuo(c.x,c.y,material,this.ejemplo(material)));
        }
        this.mensaje='Recoge un residuo y llévalo al contenedor con su mismo número.';
    }
    dirigir(d) { if(DIRECCIONES[d]) this.direccion=d; }
    get objetivo() {
        return this.carga ? `Lleva ${this.carga.ejemplo} a ${this.carga.material+1} · ${this.materiales[this.carga.material].nombre}`
            : `Recoge los residuos · Quedan ${this.residuos.length}`;
    }
    avanzar(dt) {
        this.inmune=Math.max(0,this.inmune-dt);
        for(const m of this.monstruos) m.arbol=Math.max(0,m.arbol-dt);
        this.paso+=dt; this.pasoMonstruo+=dt;
        if(this.paso>=0.13) { this.paso=0; this.moverTortuga(); }
        if(this.partida.estado!=='jugando') return;
        if(this.pasoMonstruo>=Math.max(.28,.7-this.partida.nivel*.08)) {
            this.pasoMonstruo=0;
            for(const m of this.monstruos) {
                if(m.arbol>0) continue;
                const opciones=Object.values(DIRECCIONES).map(([dx,dy])=>({x:m.x+dx,y:m.y+dy}))
                    .filter(c=>!this.esPared(c.x,c.y));
                opciones.sort((a,b)=>(Math.abs(a.x-this.tortuga.x)+Math.abs(a.y-this.tortuga.y))-(Math.abs(b.x-this.tortuga.x)+Math.abs(b.y-this.tortuga.y)));
                const c=this.aleatorio()<.65 ? opciones[0] : this.elegir(opciones);
                if(c) Object.assign(m,c);
            }
        }
        this.comprobarChoque();
    }
    moverTortuga() {
        if(!this.direccion) return;
        const [dx,dy]=DIRECCIONES[this.direccion]; const t=this.tortuga;
        if(this.esPared(t.x+dx,t.y+dy)) return;
        t.x+=dx; t.y+=dy; t.direccion=this.direccion;
        if(this.comprobarChoque()) return;
        if(!this.carga) {
            const i=this.residuos.findIndex(r=>mismo(r,t));
            if(i>=0) { this.carga=this.residuos.splice(i,1)[0]; this.ultimoResiduo={...this.carga,correcto:null}; this.mensaje=`Recogiste: ${this.carga.ejemplo}. Busca ${this.materiales[this.carga.material].nombre}.`; }
        }
        const contenedor=this.contenedores.find(c=>mismo(c,t));
        if(contenedor && this.carga) this.depositar(contenedor.material);
    }
    depositar(material) {
        if(!this.carga) return;
        if(material!==this.carga.material) {
            this.partida.sumar(-5); this.mensaje=`Ese residuo pertenece a ${this.materiales[this.carga.material].nombre}. Conservas tu carga.`; return;
        }
        this.partida.aciertos++; this.partida.sumar(25); this.mensaje=this.materiales[material].preparacion;
        this.carga=null; for(const m of this.monstruos) m.arbol=3;
        if(this.residuos.length===0) {
            this.partida.sumar(50);
            if(this.partida.nivel===4) this.ganar();
            else { this.partida.nivel++; this.crearNivel(); this.mensaje='¡Nivel superado! Ahora aparece un material más.'; }
        }
    }
    comprobarChoque() {
        if(this.inmune>0 || !this.monstruos.some(m=>m.arbol===0 && mismo(m,this.tortuga))) return false;
        this.perderVida('Un monstruo te alcanzó. Conservas el residuo; vuelve a intentarlo.');
        this.tortuga=new Tortuga(1,1); this.direccion=null; this.inmune=2;
        this.monstruos=[new Monstruo(18,1),new Monstruo(18,10)]; return true;
    }
}

export class EcoSnake extends Juego {
    constructor(materiales,aleatorio) {
        super(materiales,aleatorio); this.correctos=0; this.material=0; this.paso=0; this.reiniciarCamino(); this.generarResiduos();
    }
    reiniciarCamino() { this.tortuga=new Tortuga(7,7); this.cuerpo=[{x:6,y:7},{x:5,y:7}]; this.pendiente=null; this.colorFila=null; }
    generarResiduos() {
        this.residuos=[];
        const libres=[];
        for(let y=0;y<15;y++) for(let x=0;x<20;x++) {
            const c={x,y}; if(!mismo(c,this.tortuga) && !this.cuerpo.some(s=>mismo(s,c))) libres.push(c);
        }
        for(let m=0;m<4;m++) {
            if(!libres.length) { this.ganar(); return; }
            const c=libres.splice(Math.floor(this.aleatorio()*libres.length),1)[0];
            this.residuos.push(new Residuo(c.x,c.y,m,this.ejemplo(m)));
        }
    }
    dirigir(d) {
        if(!DIRECCIONES[d] || this.pendiente) return;
        const a=DIRECCIONES[this.tortuga.direccion],b=DIRECCIONES[d];
        if(a[0]+b[0]===0 && a[1]+b[1]===0) return;
        this.pendiente=d;
    }
    get objetivo() { return `Solo ${this.material+1} · ${this.materiales[this.material].nombre} · ${this.correctos}/12 aciertos`; }
    avanzar(dt) {
        this.paso+=dt;
        if(this.paso<Math.max(.1,.21-this.partida.nivel*.015)) return;
        this.paso=0;
        if(this.pendiente) { this.tortuga.direccion=this.pendiente; this.pendiente=null; }
        const [dx,dy]=DIRECCIONES[this.tortuga.direccion];
        const siguiente={x:this.tortuga.x+dx,y:this.tortuga.y+dy};
        const residuo=this.residuos.find(r=>mismo(r,siguiente));
        const crece=residuo?.material===this.material;
        const obstaculos=crece ? this.cuerpo : this.cuerpo.slice(0,-1);
        if(siguiente.x<0||siguiente.x>=20||siguiente.y<0||siguiente.y>=15||obstaculos.some(c=>mismo(c,siguiente))) {
            this.perderVida('Cuida a Eco: evita los bordes y tu propia fila.');
            this.reiniciarCamino(); this.generarResiduos(); return;
        }
        this.cuerpo.unshift({x:this.tortuga.x,y:this.tortuga.y});
        Object.assign(this.tortuga,siguiente); if(!crece) this.cuerpo.pop();
        if(residuo) {
            this.ultimoResiduo={...residuo,correcto:crece};
            if(crece) {
                this.colorFila=residuo.material; this.correctos++; this.partida.aciertos++; this.partida.sumar(20); this.mensaje=this.materiales[this.material].preparacion;
                if(this.correctos===12) { this.ganar(); return; }
                if(this.correctos%3===0) { this.material=(this.material+1)%4; this.partida.nivel++; this.mensaje=`Nueva ronda: recoge ${this.materiales[this.material].nombre}.`; }
            } else {
                this.partida.sumar(-10); this.perderVida(`${residuo.ejemplo} pertenece a ${this.materiales[residuo.material].nombre}. Busca ${this.materiales[this.material].nombre}.`);
            }
            this.generarResiduos();
        }
    }
}

export class EcoBreakout extends Juego {
    constructor(materiales,aleatorio) {
        super(materiales,aleatorio); this.tortuga=new Tortuga(320,442); this.ancho=110;
        this.bloques=[]; this.material=0; this.movimiento=0; this.destino=null; this.espera=1; this.sinProgreso=0; this.rescates=0;
        for(let fila=0;fila<5;fila++) for(let col=0;col<10;col++) this.bloques.push(new Bloque(35+col*58,30+fila*58,(fila+col)%4));
        this.nuevaPelota();
    }
    nuevaPelota() { this.pelota=new Pelota(this.tortuga.x,425); this.espera=1; this.sinProgreso=0; }
    orientarHaciaObjetivo() {
        const objetivos=this.bloques.filter(b=>b.activo && b.material===this.material);
        // Priorizar la fila inferior: ninguna pieza sólida oculta el objetivo.
        objetivos.sort((a,b)=>b.y-a.y || Math.abs(a.x+24-this.tortuga.x)-Math.abs(b.x+24-this.tortuga.x));
        if(!objetivos.length) return;
        const b=objetivos[this.rescates%Math.min(2,objetivos.length)];
        const dx=b.x+b.ancho/2-this.pelota.x,dy=b.y+b.alto/2-this.pelota.y;
        const longitud=Math.hypot(dx,dy);
        this.pelota.vx=300*dx/longitud; this.pelota.vy=300*dy/longitud;
    }
    corregirTrayectoria() {
        const p=this.pelota,rapidez=300;
        // Evita órbitas perfectamente verticales u horizontales tras los rebotes.
        if(Math.abs(p.vx)<75) p.vx=(p.vx<0?-1:1)*75;
        if(Math.abs(p.vy)<135) p.vy=(p.vy<0?-1:1)*135;
        const norma=Math.hypot(p.vx,p.vy); p.vx=p.vx/norma*rapidez; p.vy=p.vy/norma*rapidez;
    }
    dirigir(d) { this.movimiento=d==='izquierda'?-1:d==='derecha'?1:0; this.destino=null; }
    apuntar(x) { this.destino=limitar(x,this.ancho/2,640-this.ancho/2); this.movimiento=0; }
    get objetivo() { return `Rompe ${this.material+1} · ${this.materiales[this.material].nombre} · Quedan ${this.bloques.filter(b=>b.activo).length} bloques`; }
    golpear(b) {
        if(!b.activo || b.material!==this.material) return; // Los otros materiales son atravesables en esta ronda.
        b.activo=false; this.partida.aciertos++; this.sinProgreso=0; this.ultimoResiduo={material:b.material,ejemplo:this.materiales[b.material].nombre,correcto:true}; this.partida.sumar(15); this.mensaje=this.materiales[b.material].preparacion;
        const restantes=this.bloques.filter(b=>b.activo);
        if(!restantes.length) { this.ganar(); return; }
        if(!restantes.some(b=>b.material===this.material)) {
            this.material=restantes[0].material; this.partida.nivel++;
            this.mensaje=`¡Material completado! Ahora: ${this.materiales[this.material].nombre}.`;
        }
    }
    avanzar(dt) {
        // Subpasos para que la pelota no atraviese bloques al bajar los FPS.
        const pasos=Math.max(1,Math.ceil(dt/.008));
        for(let i=0;i<pasos && this.partida.estado==='jugando';i++) this.fisica(dt/pasos);
    }
    fisica(dt) {
        const antes=this.tortuga.x;
        const destino=this.destino ?? antes+this.movimiento*430*dt;
        this.tortuga.x=limitar(antes+limitar(destino-antes,-430*dt,430*dt),this.ancho/2,640-this.ancho/2);
        const p=this.pelota;
        if(this.espera>0) {
            this.espera-=dt; p.x=this.tortuga.x;
            if(this.espera<=0) this.orientarHaciaObjetivo();
            return;
        }
        this.sinProgreso+=dt;
        if(this.sinProgreso>=12) {
            this.rescates++; this.nuevaPelota();
            this.mensaje='Eco relanza la pelota hacia un bloque pendiente. Conservas tus vidas y puntos.';
            return;
        }
        const anterior={x:p.x,y:p.y}; p.x+=p.vx*dt; p.y+=p.vy*dt;
        if(p.x<p.radio) { p.x=p.radio; p.vx=Math.abs(p.vx); }
        if(p.x>640-p.radio) { p.x=640-p.radio; p.vx=-Math.abs(p.vx); }
        if(p.y<p.radio) { p.y=p.radio; p.vy=Math.abs(p.vy); }
        if(p.vy>0 && anterior.y+p.radio<=this.tortuga.y && p.y+p.radio>=this.tortuga.y && Math.abs(p.x-this.tortuga.x)<=this.ancho/2+p.radio) {
            const angulo=limitar((p.x-this.tortuga.x)/(this.ancho/2),-.95,.95)*1.05;
            p.vx=300*Math.sin(angulo); p.vy=-300*Math.cos(angulo); this.corregirTrayectoria(); p.y=this.tortuga.y-p.radio-1;
        }
        const b=this.bloques.find(b=>b.activo && b.material===this.material && p.x+p.radio>b.x && p.x-p.radio<b.x+b.ancho && p.y+p.radio>b.y && p.y-p.radio<b.y+b.alto);
        if(b) {
            if(anterior.y+p.radio<=b.y || anterior.y-p.radio>=b.y+b.alto) { p.vy*=-1; p.y=anterior.y; }
            else { p.vx*=-1; p.x=anterior.x; }
            this.golpear(b);
        }
        if(p.y>490) { this.perderVida('La pelota cayó. Eco tiene otra oportunidad.'); this.nuevaPelota(); }
    }
}
