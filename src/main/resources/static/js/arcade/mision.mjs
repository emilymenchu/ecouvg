/** La confirmación visible procede del servidor; el contador de juego es local. */
export class MisionRacha {
    constructor({url,csrf,header,notificar,solicitar=fetch}) {
        this.url=url;this.csrf=csrf;this.header=header;this.notificar=notificar;this.solicitar=solicitar;
        this.vistas=new WeakSet();this.estado=null;this.pendiente=null;this.enviando=false;this.error=false;
    }
    async peticion(opciones={}) {
        const respuesta=await this.solicitar(this.url,{...opciones,credentials:'same-origin',cache:'no-store',signal:AbortSignal.timeout(10000)});
        if(respuesta.redirected||respuesta.status===401||respuesta.status===403) throw new Error('La sesión o el permiso de envío expiró. Inicia sesión de nuevo antes de guardar.');
        if(!respuesta.ok) throw new Error('No se guardó la misión. Reintenta cuando vuelva la conexión.');
        const datos=await respuesta.json();
        if(typeof datos.completada!=='boolean'||!Number.isInteger(datos.semanasRacha)) throw new Error('Respuesta inválida. No se confirmó la misión.');
        return datos;
    }
    mostrar(texto,reintentar=false) { this.notificar(texto,reintentar); }
    async cargar() {
        if(this.enviando) return;
        this.enviando=true;this.error=false;
        try {
            this.estado=await this.peticion({headers:{Accept:'application/json'}});
            if(this.estado.completada) this.pendiente=null;
            this.mostrar(this.estado.completada?`✓ Misión de esta semana completada · Racha: ${this.estado.semanasRacha} semanas.`:'Consigue 5 aciertos en una partida para aportar a tu racha semanal.');
        } catch(e) {this.error=true;this.mostrar(e.message,true);}
        finally {this.enviando=false;}
        if(!this.error&&this.pendiente&&!this.estado.completada) await this.guardar();
    }
    observar(juego,partida) {
        if(partida.aciertos<5||this.vistas.has(partida)) return;
        this.vistas.add(partida);
        if(this.pendiente) return;
        // Copia el hito antes de que se reinicie o cambie la partida.
        this.pendiente={juego,aciertos:5};
        if(this.estado?.completada) { void this.cargar(); return; }
        if(this.estado&&!this.error) void this.guardar();
    }
    async guardar() {
        if(this.enviando||!this.pendiente||this.estado?.completada) return;
        this.enviando=true;this.error=false;this.mostrar('5 aciertos alcanzados. Guardando misión…');
        try {
            if(!this.csrf||!this.header) throw new Error('Recarga la página para obtener el permiso de envío.');
            this.estado=await this.peticion({method:'POST',headers:{'Content-Type':'application/json',Accept:'application/json',[this.header]:this.csrf},body:JSON.stringify(this.pendiente)});
            if(!this.estado.completada) throw new Error('El servidor no confirmó la misión. Reintenta.');
            this.pendiente=null;
            this.mostrar(`✓ ¡Misión completada! Tu actividad semanal está registrada · Racha: ${this.estado.semanasRacha} semanas.`);
        } catch(e) {this.error=true;this.mostrar(e.message,true);}
        finally {this.enviando=false;}
    }
    async reintentar() { if(this.pendiente&&this.estado) await this.guardar();else await this.cargar(); }
}
