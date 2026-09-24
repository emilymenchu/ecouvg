package gt.edu.uvg.ecouvg.modelo;
import jakarta.persistence.*;
import java.util.ArrayList; 

@Entity
@Table(name = "contenedor")
public class Contenedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre; 
    private String color; 
    private String descripcion; 
    @ManyToMany
    private ArrayList<Residuo> residuosAceptados = new ArrayList<>(); 

    public Contenedor(){

    }

    public Contenedor(String nombre, String color, String descripcion, ArrayList<Residuo> residuosAceptados){
        this.nombre = nombre; 
        this.color = color; 
        this.descripcion = descripcion; 
        this.residuosAceptados = residuosAceptados; 
    }

    public Long getId(){
        return id; 
    }

    public String getNombre(){
        return nombre; 
    }

    public String getColor(){
        return color; 
    }

    public String getDescripcion(){
        return descripcion; 
    }

    public boolean aceptaResiduo(Residuo residuo){
        return residuosAceptados.contains(residuo);
    }


    public ArrayList<Residuo> getResiduosAceptados(){
        return residuosAceptados ;
    }
    
}
