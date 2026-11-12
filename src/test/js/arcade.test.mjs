import test from 'node:test';
import assert from 'node:assert/strict';
import {EcoRun,EcoSnake,EcoBreakout,Residuo,DIRECCIONES} from '../../main/resources/static/js/arcade/modelo.mjs';
const materiales=['Plásticos','Metales','Papel','Cartón'].map((nombre,i)=>({nombre,slug:String(i),ejemplos:[`Ejemplo ${i}`],preparacion:`Preparación ${i}`}));
const azar=()=>.42;
for(const Clase of [EcoRun,EcoSnake,EcoBreakout]) {
    test(`${Clase.name}: inicio, pausa, fin e independencia entre partidas`,()=>{
        const j=new Clase(materiales,azar);assert.equal(j.partida.estado,'lista');
        j.actualizar(1);assert.equal(j.tiempo,0);j.iniciar();j.actualizar(.02);assert.ok(j.tiempo>0);
        j.pausar();const t=j.tiempo;j.actualizar(10);assert.equal(j.tiempo,t);j.continuar();
        j.perderVida('a');j.perderVida('b');j.perderVida('c');assert.equal(j.partida.estado,'fin');
        j.actualizar(1);assert.equal(j.tiempo,t);assert.equal(new Clase(materiales).partida.vidas,3);
        j.partida.sumar(-500);assert.equal(j.partida.puntos,0);
    });
}
test('Eco Run: todo residuo, contenedor y enemigo es alcanzable',()=>{
    for(let nivel=1;nivel<=4;nivel++) {
        const j=new EcoRun(materiales,azar);j.partida.nivel=nivel;j.crearNivel();
        const vistos=new Set(['1,1']),cola=[{x:1,y:1}];
        while(cola.length) {const c=cola.shift();for(const [dx,dy] of Object.values(DIRECCIONES)) {
            const x=c.x+dx,y=c.y+dy,k=`${x},${y}`;
            if(!j.esPared(x,y)&&!vistos.has(k)){vistos.add(k);cola.push({x,y});}
        }}
        for(const c of [...j.residuos,...j.contenedores,...j.monstruos]) assert.ok(vistos.has(`${c.x},${c.y}`));
        assert.equal(j.residuos.length,nivel*2);
        assert.equal(new Set(j.residuos.map(r=>`${r.x},${r.y}`)).size,nivel*2);
    }
});
test('Eco Run: clasificar conserva carga equivocada, transforma monstruos y completa cuatro niveles',()=>{
    const j=new EcoRun(materiales,azar);j.iniciar();
    j.carga=new Residuo(1,1,0,'Botella');j.partida.sumar(10);j.depositar(1);
    assert.ok(j.carga);assert.equal(j.partida.puntos,5);
    j.depositar(0);assert.equal(j.carga,null);assert.ok(j.monstruos.every(m=>m.arbol===3));
    for(let nivel=1;nivel<=4;nivel++) {
        const residuos=[...j.residuos];j.residuos=[];
        // Mantener un residuo pendiente hasta la última entrega del nivel.
        for(let i=0;i<residuos.length;i++){j.residuos=residuos.slice(i+1);j.carga=residuos[i];j.depositar(j.carga.material);}
    }
    assert.equal(j.partida.estado,'victoria');
});
test('Eco Run: colisión quita una vida y respeta inmunidad',()=>{
    const j=new EcoRun(materiales);j.inmune=0;j.monstruos[0].x=1;j.monstruos[0].y=1;
    assert.equal(j.comprobarChoque(),true);assert.equal(j.partida.vidas,2);
    assert.equal(j.comprobarChoque(),false);assert.equal(j.partida.vidas,2);
});
test('Snake: no se puede invertir dirección ni encadenar dos giros en un paso',()=>{
    const j=new EcoSnake(materiales);j.dirigir('izquierda');assert.equal(j.pendiente,null);
    j.dirigir('arriba');j.dirigir('izquierda');assert.equal(j.pendiente,'arriba');
});
test('Snake: aciertos crecen y completan las cuatro rondas',()=>{
    const j=new EcoSnake(materiales,azar);j.iniciar();
    for(let n=0;n<12;n++) {
        j.reiniciarCamino();j.residuos=[new Residuo(8,7,j.material,'Ejemplo')];j.actualizar(.3);
        assert.equal(j.cuerpo.length,3);
    }
    assert.equal(j.partida.estado,'victoria');assert.equal(j.partida.puntos,240);
});
test('Snake: material incorrecto y bordes cuestan vidas',()=>{
    const j=new EcoSnake(materiales,azar);j.iniciar();j.residuos=[new Residuo(8,7,1,'Lata')];j.actualizar(.3);
    assert.equal(j.partida.vidas,2);assert.equal(j.correctos,0);
    j.tortuga.x=19;j.actualizar(.3);assert.equal(j.partida.vidas,1);
});
test('Snake: generación nunca coloca residuos sobre la tortuga ni entre sí',()=>{
    const j=new EcoSnake(materiales,azar);
    assert.equal(new Set(j.residuos.map(r=>`${r.x},${r.y}`)).size,4);
    assert.ok(j.residuos.every(r=>![[j.tortuga.x,j.tortuga.y],...j.cuerpo.map(c=>[c.x,c.y])].some(([x,y])=>x===r.x&&y===r.y)));
});
test('Breakout: materiales futuros no penalizan ni desaparecen, fases pueden completarse',()=>{
    const j=new EcoBreakout(materiales);j.iniciar();j.partida.sumar(10);
    const incorrecto=j.bloques.find(b=>b.material===1);j.golpear(incorrecto);
    assert.equal(incorrecto.activo,true);assert.equal(j.partida.puntos,10);
    while(j.bloques.some(b=>b.activo)) j.golpear(j.bloques.find(b=>b.activo&&b.material===j.material));
    assert.equal(j.partida.estado,'victoria');
});
test('Breakout: rebota en plataforma y pared, y pierde vida al caer',()=>{
    const j=new EcoBreakout(materiales);j.iniciar();j.espera=0;
    j.pelota.x=j.tortuga.x;j.pelota.y=430;j.pelota.vy=200;j.actualizar(.04);
    assert.ok(j.pelota.vy<0);j.pelota.x=8;j.pelota.vx=-200;j.actualizar(.02);assert.ok(j.pelota.vx>0);
    j.pelota.y=492;j.pelota.x=0;j.actualizar(.01);assert.equal(j.partida.vidas,2);assert.ok(j.espera>0);
});
test('Breakout: subpasos detectan bloque a alta velocidad',()=>{
    const j=new EcoBreakout(materiales);j.iniciar();j.espera=0;
    const b=j.bloques.find(b=>b.material===j.material);j.pelota.x=b.x+45;j.pelota.y=b.y+b.alto+12;j.pelota.vy=-500;j.pelota.vx=0;
    j.actualizar(.05);assert.equal(b.activo,false);
});

test('Snake: recogida conserva el nombre y colorea la fila aunque cambie el objetivo',()=>{
    const j=new EcoSnake(materiales,azar);j.iniciar();j.correctos=2;
    j.residuos=[new Residuo(8,7,0,'Botella de agua')];j.actualizar(.3);
    assert.equal(j.material,1);assert.equal(j.colorFila,0);
    assert.equal(j.ultimoResiduo.ejemplo,'Botella de agua');assert.equal(j.ultimoResiduo.correcto,true);
});
test('Snake: la identificación del residuo equivocado queda visible',()=>{
    const j=new EcoSnake(materiales,azar);j.iniciar();j.residuos=[new Residuo(8,7,2,'Hoja')];j.actualizar(.3);
    assert.equal(j.ultimoResiduo.material,2);assert.equal(j.ultimoResiduo.correcto,false);assert.equal(j.colorFila,null);
});
test('Eco Run: identifica la carga y el último residuo por su ejemplo',()=>{
    const j=new EcoRun(materiales,azar);j.iniciar();j.inmune=5;
    j.residuos=[new Residuo(2,1,1,'Lata de bebida')];j.dirigir('derecha');j.actualizar(.14);
    assert.equal(j.carga.ejemplo,'Lata de bebida');assert.equal(j.ultimoResiduo.ejemplo,'Lata de bebida');
});
test('Breakout: 50 cuadrados separados y dentro del tablero',()=>{
    const j=new EcoBreakout(materiales);assert.equal(j.bloques.length,50);
    for(const b of j.bloques){assert.equal(b.ancho,b.alto);assert.ok(b.x>=0&&b.x+b.ancho<=640&&b.y+b.alto<400);}
    for(let i=0;i<j.bloques.length;i++)for(let k=i+1;k<j.bloques.length;k++){
        const a=j.bloques[i],b=j.bloques[k];assert.ok(a.x+a.ancho<=b.x||b.x+b.ancho<=a.x||a.y+a.alto<=b.y||b.y+b.alto<=a.y);
    }
});
test('Breakout: la pelota atraviesa materiales futuros sin rebote ni penalización',()=>{
    const j=new EcoBreakout(materiales);j.iniciar();j.espera=0;
    const b=j.bloques.find(b=>b.material===1);j.bloques=[b,...j.bloques.filter(x=>x.material===0)];
    j.pelota.x=b.x+24;j.pelota.y=b.y+b.alto+10;j.pelota.vy=-250;j.pelota.vx=0;
    j.actualizar(.1);assert.ok(j.pelota.vy<0);assert.equal(b.activo,true);assert.equal(j.partida.puntos,0);
});
test('Breakout: rebote central tiene componente horizontal y vertical suficientes',()=>{
    const j=new EcoBreakout(materiales);j.iniciar();j.espera=0;
    j.pelota.x=j.tortuga.x;j.pelota.y=430;j.pelota.vx=0;j.pelota.vy=200;
    j.actualizar(.04);assert.ok(Math.abs(j.pelota.vx)>=65);assert.ok(j.pelota.vy<-135);
});
test('Breakout: bucle sin progreso se relanza sin gastar vidas o puntos',()=>{
    const j=new EcoBreakout(materiales);j.iniciar();j.espera=0;j.sinProgreso=11.99;
    j.partida.sumar(150);j.pelota.y=350;j.pelota.vx=250;j.pelota.vy=0;
    j.actualizar(.03);assert.equal(j.rescates,1);assert.ok(j.espera>0);
    assert.equal(j.partida.vidas,3);assert.equal(j.partida.puntos,150);
    j.pausar();const espera=j.espera;j.actualizar(100);assert.equal(j.espera,espera);
});
test('Breakout: último bloque rodeado de otros materiales sigue alcanzable tras relanzamiento',()=>{
    const j=new EcoBreakout(materiales);j.iniciar();
    // Un objetivo alto que antes quedaba oculto por bloques de rondas futuras.
    const ultimo=j.bloques.find(b=>b.material===0&&b.y===30);
    for(const b of j.bloques)if(b.material===0&&b!==ultimo)b.activo=false;
    j.sinProgreso=12;j.espera=0;j.actualizar(.01);
    for(let i=0;i<300&&ultimo.activo;i++)j.actualizar(.016);
    assert.equal(ultimo.activo,false);assert.notEqual(j.material,0);
});
test('Breakout: simulación física completa elimina los 50 bloques sin bloqueo',()=>{
    const j=new EcoBreakout(materiales);j.iniciar();
    for(let i=0;i<20000 && j.partida.estado==='jugando';i++) {
        j.apuntar(j.pelota.x);j.actualizar(.016);
    }
    assert.equal(j.partida.estado,'victoria');assert.equal(j.bloques.filter(b=>b.activo).length,0);
});
