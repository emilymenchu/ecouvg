package gt.edu.uvg.ecouvg.modelo;

public class CalculadorNivel {
    
    public int calculadorNivel(int xp){
        
        if(xp<0){
            throw new IllegalArgumentException("No se puede tener XP negativos : ( ") ;
        }else if (xp>= 10000) {
            return 6; 
            
        }else if (xp>= 1000){
            return 5; 
        }else if(xp>= 500){
            return 4; 
        }else if (xp>= 250){
            return 3; 
        }else if(xp>=100){
            return 2; 
        }else{
            return 1; 
        }
    }

    public String nombreNivel(int nivel){
        switch(nivel) {
            case 1: 
                return "Semilla" ;
            case 2: 
                return "Brote"; 
            case 3: 
                return "Eco Explorador"; 
            case 4:
                return "Eco Guardían"; 
            case 5: 
                return "Eco Lider" ;
            case 6: 
                return "Terraformador"; 
            default: 
            return "Nivel no encontrado"; 
            
        }
    }

    

}
