import test from 'node:test';
import assert from 'node:assert/strict';
import {EcoRun,EcoSnake,EcoBreakout,Residuo} from '../../main/resources/static/js/arcade/modelo.mjs';
import {MisionRacha} from '../../main/resources/static/js/arcade/mision.mjs';
const materiales=['Plástico','Metal','Papel','Cartón'].map(nombre=>({nombre,ejemplos:[nombre],preparacion:'Preparar'}));
const tick=()=>new Promise(r=>setImmediate(r));
test('Eco Run cuenta entregas, no recogidas ni puntos de nivel; conserva cinco entre niveles',()=>{
 const j=new EcoRun(materiales);j.iniciar();
 for(let i=0;i<5;i++) {const r=j.residuos.shift();j.carga=r;j.depositar((r.material+1)%4);assert.equal(j.partida.aciertos,i);j.depositar(r.material);}
 assert.equal(j.partida.aciertos,5);assert.equal(j.partida.nivel,2);assert.equal(new EcoRun(materiales).partida.aciertos,0);
});
test('Snake cuenta cinco aciertos aunque cambie de material y haya errores',()=>{
 const j=new EcoSnake(materiales);j.iniciar();
 for(let i=0;i<5;i++){j.reiniciarCamino();j.residuos=[new Residuo(8,7,j.material,'Residuo')];j.actualizar(.3);}
 assert.equal(j.partida.aciertos,5);j.reiniciarCamino();j.residuos=[new Residuo(8,7,(j.material+1)%4,'Otro')];j.actualizar(.3);assert.equal(j.partida.aciertos,5);
});
test('Breakout cuenta solamente cinco bloques correctos distintos',()=>{
 const j=new EcoBreakout(materiales);const otros=j.bloques.find(b=>b.material===1);j.golpear(otros);assert.equal(j.partida.aciertos,0);
 const objetivos=j.bloques.filter(b=>b.material===0).slice(0,5);
 for(const b of objetivos){j.golpear(b);j.golpear(b);}assert.equal(j.partida.aciertos,5);
});
function crear(fallo=false) {
 const llamadas=[],avisos=[];let fallar=fallo;
 const m=new MisionRacha({url:'/api/arcade/mision',csrf:'token',header:'X-CSRF-TOKEN',notificar:(t,r)=>avisos.push([t,r]),solicitar:async(url,op)=>{
  llamadas.push(op);if(op.method==='POST'&&fallar){fallar=false;throw Error('Sin conexión');}
  return {ok:true,status:200,json:async()=>({completada:op.method==='POST',semanasRacha:2,objetivo:5,semana:'2026-11-09'})};
 }});return {m,llamadas,avisos};
}
test('No envía con cuatro, envía una sola vez al quinto y lleva CSRF',async()=>{
 const {m,llamadas}=crear();await m.cargar();const p={aciertos:4};m.observar('eco-run',p);assert.equal(llamadas.length,1);
 p.aciertos=5;m.observar('eco-run',p);m.observar('eco-run',p);await tick();
 assert.equal(llamadas.length,2);assert.equal(llamadas[1].headers['X-CSRF-TOKEN'],'token');assert.equal(m.estado.completada,true);m.observar('eco-run',p);assert.equal(llamadas.length,2);
});
test('Error de red no confirma éxito ni provoca un POST en cada fotograma; permite reintento',async()=>{
 const {m,llamadas,avisos}=crear(true);await m.cargar();const p={aciertos:5};m.observar('eco-snake',p);await tick();
 assert.equal(m.estado.completada,false);assert.equal(avisos.at(-1)[1],true);
 for(let i=0;i<100;i++)m.observar('eco-snake',p);assert.equal(llamadas.length,2);
 await m.reintentar();assert.equal(m.estado.completada,true);assert.equal(llamadas.length,3);
});
test('Reiniciar o cambiar juego no suma aciertos parciales de partidas distintas',async()=>{
 const {m,llamadas}=crear();await m.cargar();m.observar('eco-run',{aciertos:3});m.observar('eco-snake',{aciertos:2});assert.equal(llamadas.length,1);
});
test('Al cambiar semana no reutiliza una partida que ya fue acreditada',async()=>{
 const {m,llamadas}=crear();await m.cargar();const p={aciertos:5};m.observar('eco-run',p);await tick();
 await m.cargar();m.observar('eco-run',p);assert.equal(llamadas.filter(o=>o.method==='POST').length,1);
 m.observar('eco-run',{aciertos:5});await tick();assert.equal(llamadas.filter(o=>o.method==='POST').length,2);
});
