package gt.edu.uvg.ecouvg.dto;

public class SolicitudChat {

    private String pregunta;

    public SolicitudChat() {
    }

    public SolicitudChat(String pregunta) {
        this.pregunta = pregunta;
    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }
}
