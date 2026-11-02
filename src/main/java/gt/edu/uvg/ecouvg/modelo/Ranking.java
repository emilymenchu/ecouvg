package gt.edu.uvg.ecouvg.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Ranking implements Iterable<Usuario>{
    private List<Usuario> usuarios;
    private int nivel;
    
    public Ranking(int nivel) {
        this.nivel = nivel;
        this.usuarios = new ArrayList<>();
    }
    

    public void agregarUsuario(Usuario usuario) {
        if (usuario.getNivel() == nivel) {
            usuarios.add(usuario);
        }
    }

    public void ordenar() {
        Collections.sort(usuarios);
    }

    public Iterator<Usuario> iterator() {
        return usuarios.iterator();
    }
}