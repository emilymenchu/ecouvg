import {EcoRun,EcoSnake,EcoBreakout} from './modelo.mjs';
export const PALETA=['#71c9ec','#d0d9de','#f2d777','#dca477'];
export class Dibujante {
    constructor(canvas) { this.canvas=canvas; this.ctx=canvas.getContext('2d'); }
    rect(x,y,w,h,color,r=5) {
        const c=this.ctx; c.fillStyle=color; c.beginPath(); c.roundRect(x,y,w,h,r); c.fill();
    }
    texto(texto,x,y,tamano=16,color='#ecf8e9',alineacion='center') {
        const c=this.ctx; c.fillStyle=color; c.font=`700 ${tamano}px system-ui`; c.textAlign=alineacion; c.fillText(texto,x,y);
    }
    tortuga(x,y,tamano=28,direccion='derecha') {
        const c=this.ctx; c.save(); c.translate(x,y);
        c.rotate(({derecha:0,abajo:Math.PI/2,izquierda:Math.PI,arriba:-Math.PI/2})[direccion]);
        c.scale(tamano/28,tamano/28);
        this.rect(-11,-12,8,7,'#acd66c'); this.rect(-11,5,8,7,'#acd66c');
        this.rect(1,-12,8,7,'#acd66c'); this.rect(1,5,8,7,'#acd66c');
        this.rect(7,-6,12,12,'#c9e78c',5); this.rect(-13,-10,23,20,'#163d29',9);
        this.rect(-10,-8,18,16,'#78a743',7); this.rect(-6,-5,10,10,'#b2cd68',4);
        this.rect(13,-4,3,3,'#183b2b',1); c.restore();
    }
    arbol(x,y) { this.rect(x-3,y,6,14,'#bc9a6c'); this.rect(x-13,y-14,26,22,'#90d37b',10); }
    iconoResiduo(material,x,y,tamano=32) {
        const c=this.ctx; c.save(); c.translate(x,y); c.scale(tamano/40,tamano/40);
        const tinta='#18382c',color=PALETA[material];
        // Siluetas diferentes: botella, lata, hoja y caja; el color nunca es la única pista.
        if(material===0) {
            this.rect(-5,-17,10,6,tinta,2);this.rect(-8,-12,16,6,color,3);
            this.rect(-10,-7,20,24,tinta,5);this.rect(-8,-5,16,20,color,4);
            this.rect(-8,0,16,9,'#f2fcff',1);this.rect(-5,2,10,5,tinta,1);
        }
        if(material===1) {
            this.rect(-12,-16,24,33,tinta,6);this.rect(-10,-12,20,26,color,3);
            this.rect(-9,-14,18,5,'#fff',3);this.rect(-3,-13,6,3,tinta,1);
            this.rect(-7,-3,14,9,'#f7fbfc',2);this.rect(-9,11,18,3,tinta,1);
        }
        if(material===2) {
            this.rect(-13,-17,26,34,tinta,2);this.rect(-11,-15,22,30,'#fff4bf',1);
            for(let i=0;i<4;i++) this.rect(-7,-7+i*5,14,2,'#705820',1);
            this.rect(5,-15,6,6,color,0);
        }
        if(material===3) {
            this.rect(-15,-13,30,29,tinta,2);this.rect(-13,-11,26,25,color,1);
            this.rect(-3,-11,6,25,'#ffe2b5',0);this.rect(-13,-4,26,3,tinta,0);
            this.rect(5,4,5,6,tinta,1);
        }
        c.restore();
    }
    residuo(r,objetivo=false) {
        const x=r.x*32+16,y=r.y*32+16;
        this.rect(x-15,y-15,30,30,PALETA[r.material],6);
        if(objetivo) { this.ctx.strokeStyle='#fff';this.ctx.lineWidth=2;this.ctx.strokeRect(x-15,y-15,30,30); }
        this.iconoResiduo(r.material,x,y,27);
        this.rect(x+6,y-16,11,12,'#fff',3);this.texto(String(r.material+1),x+11.5,y-6,10,'#17372b');
    }
    dibujar(juego) {
        const c=this.ctx; this.rect(0,0,640,480,'#102f26',0);
        c.strokeStyle='#ffffff08'; c.lineWidth=1;
        for(let x=0;x<640;x+=32) { c.beginPath();c.moveTo(x,0);c.lineTo(x,480);c.stroke(); }
        for(let y=0;y<480;y+=32) { c.beginPath();c.moveTo(0,y);c.lineTo(640,y);c.stroke(); }
        if(juego instanceof EcoRun) this.ecoRun(juego);
        else if(juego instanceof EcoSnake) this.snake(juego);
        else if(juego instanceof EcoBreakout) this.breakout(juego);
        if(juego.partida.estado==='victoria') {
            this.rect(0,0,640,480,'#c9e4b0',0);
            for(let y=90;y<400;y+=80) for(let x=60;x<630;x+=90) this.arbol(x,y);
            this.tortuga(320,260,75); this.texto('UN PLANETA MÁS VERDE',320,55,24,'#173c2c');
        }
    }
    ecoRun(j) {
        for(let y=0;y<15;y++) for(let x=0;x<20;x++) if(j.esPared(x,y)) {
            this.rect(x*32+2,y*32+2,28,28,'#315b43',6); this.rect(x*32+5,y*32+4,22,3,'#527c53',2);
        }
        for(const b of j.contenedores) { this.rect(b.x*32+1,b.y*32,30,30,PALETA[b.material]); this.iconoResiduo(b.material,b.x*32+16,b.y*32+16,24); this.texto(String(b.material+1),b.x*32+27,b.y*32+11,10,'#18382c'); }
        for(const r of j.residuos) this.residuo(r);
        for(const m of j.monstruos) {
            const x=m.x*32+16,y=m.y*32+16;
            if(m.arbol>0) this.arbol(x,y);
            else { this.rect(x-12,y-12,24,25,'#c38286',7); this.rect(x-7,y-5,5,6,'#fff',2); this.rect(x+3,y-5,5,6,'#fff',2); }
        }
        if(j.inmune<=0 || Math.floor(j.inmune*8)%2===0) this.tortuga(j.tortuga.x*32+16,j.tortuga.y*32+16,27,j.tortuga.direccion);
        if(j.carga) { this.ctx.strokeStyle=PALETA[j.carga.material];this.ctx.lineWidth=3;this.ctx.strokeRect(j.tortuga.x*32+1,j.tortuga.y*32+1,30,30); }
    }
    snake(j) {
        for(const s of j.cuerpo) { this.rect(s.x*32+3,s.y*32+3,26,26,j.colorFila===null?'#648b43':PALETA[j.colorFila],9); this.rect(s.x*32+11,s.y*32+11,10,10,'#234e3a',5); }
        for(const r of j.residuos) this.residuo(r,r.material===j.material);
        this.ctx.strokeStyle=PALETA[j.material];this.ctx.lineWidth=3;this.ctx.strokeRect(j.tortuga.x*32+1,j.tortuga.y*32+1,30,30);
        this.tortuga(j.tortuga.x*32+16,j.tortuga.y*32+16,28,j.tortuga.direccion);
    }
    breakout(j) {
        for(const b of j.bloques) if(b.activo) {
            const actual=b.material===j.material;
            this.ctx.save();this.ctx.globalAlpha=actual?1:.22;
            this.rect(b.x,b.y,b.ancho,b.alto,PALETA[b.material],7);
            this.iconoResiduo(b.material,b.x+24,b.y+24,36);
            this.texto(String(b.material+1),b.x+40,b.y+12,11,'#18382c');
            if(actual) { this.ctx.strokeStyle='#fff';this.ctx.lineWidth=2;this.ctx.strokeRect(b.x-2,b.y-2,b.ancho+4,b.alto+4); }
            this.ctx.restore();
        }
        this.rect(j.tortuga.x-j.ancho/2,j.tortuga.y,j.ancho,12,PALETA[j.material],6);
        this.tortuga(j.tortuga.x,j.tortuga.y-12,33,'arriba');
        this.ctx.fillStyle=PALETA[j.material];this.ctx.beginPath();this.ctx.arc(j.pelota.x,j.pelota.y,j.pelota.radio,0,2*Math.PI);this.ctx.fill();
    }
}
