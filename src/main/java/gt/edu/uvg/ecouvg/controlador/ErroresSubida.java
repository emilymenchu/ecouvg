package gt.edu.uvg.ecouvg.controlador;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** El tamaño puede rechazarse antes de resolver el controlador de la petición. */
@ControllerAdvice
public class ErroresSubida {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String fotografiaDemasiadoGrande(HttpServletRequest solicitud, RedirectAttributes atributos) {
        atributos.addFlashAttribute("error", "La fotografía no debe superar los 4 MB.");
        boolean perfil = solicitud.getRequestURI().equals(solicitud.getContextPath() + "/perfil/foto");
        return perfil ? "redirect:/perfil" : "redirect:/retos";
    }
}
