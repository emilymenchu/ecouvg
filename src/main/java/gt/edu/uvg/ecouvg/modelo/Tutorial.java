
package gt.edu.uvg.ecouvg.modelo;
import jakarta.persistence.*;

@Entity
@Table(name = "tutorial")
public class Tutorial {
    //Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 
    private String titulo; 
    private String descripcion; 
    private String contenido; 
    private String categoria;

    public Tutorial(){

    }

    public Tutorial(String titulo, String descripcion, String contenido, String categoria){
        this.titulo = titulo; 
        this.descripcion = descripcion; 
        this.contenido = contenido; 
        this.categoria = categoria;
    }

    public Long getId(){
        return id;
    }

    public String getTitulo(){
        return titulo; 
    }

    public String getDescripcion(){
        return descripcion; 
    }

    public String getContenido(){
        return contenido; 
    }

    public String getCategoria(){
        return categoria; 
    }
}
