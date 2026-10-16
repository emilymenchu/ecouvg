package gt.edu.uvg.ecouvg.servicio;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ServicioCorreo {

    private final JavaMailSender mailSender;

    public ServicioCorreo(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCodigoRecuperacion(String correo, String codigo) {

        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setTo(correo);
        mensaje.setSubject("Recuperación de contraseña - EcoUVG");

        mensaje.setText(
            "Solicitaste recuperar tu contraseña de EcoUVG.\n\n" +
            "Tu código de recuperación es:\n\n" +
            codigo +
            "\n\nEste código expira en 10 minutos."
        );

        mailSender.send(mensaje);
    }
}