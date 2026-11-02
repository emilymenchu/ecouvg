package gt.edu.uvg.ecouvg.dto;

public class SolicitudChat {

    private String pregunta;
    private java.util.UUID conversacionId;
    private java.util.UUID solicitudId;

    public java.util.UUID getConversacionId() { return conversacionId; }
    public void setConversacionId(java.util.UUID id) { conversacionId = id; }
    public java.util.UUID getSolicitudId() { return solicitudId; }
    public void setSolicitudId(java.util.UUID id) { solicitudId = id; }

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
