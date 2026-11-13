package gt.edu.uvg.ecouvg.controlador;

import java.time.LocalDate;
import java.time.ZoneId; 

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.servicio.ServicioUsuario;
import gt.edu.uvg.ecouvg.servicio.ServicioSocial;
import gt.edu.uvg.ecouvg.modelo.Reto;
import gt.edu.uvg.ecouvg.modelo.ProgresoReto;
import gt.edu.uvg.ecouvg.servicio.ServicioReto;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class ControladorLogin {

    private final ServicioUsuario servicioUsuario;
    private final ServicioSocial servicioSocial;
    private final ServicioReto servicioReto;

    public ControladorLogin(ServicioUsuario servicioUsuario, ServicioSocial servicioSocial, ServicioReto servicioReto) {
        this.servicioUsuario = servicioUsuario;
        this.servicioSocial = servicioSocial;
        this.servicioReto = servicioReto;
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(
            @RequestParam String nombre,
            @RequestParam String carnet,
            @RequestParam String correo,
            @RequestParam String contrasena,
            @RequestParam String confirmarContrasena,
            Model model) {

        if (!contrasena.equals(confirmarContrasena)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "registro";
        }

        if (servicioUsuario.carnetExiste(carnet)) {
            model.addAttribute("error", "Ya existe una cuenta con ese carné.");
            return "registro";
        }

        if (servicioUsuario.correoExiste(correo)) {
            model.addAttribute("error", "Ya existe una cuenta con ese correo.");
            return "registro";
        }

        servicioUsuario.registrarUsuario(nombre, carnet, correo, contrasena);

        return "redirect:/login?registro=true";
    }

    @GetMapping("/inicio")
    public String mostrarInicio(Authentication authentication, Model model) {

        String carnet = authentication.getName();

        Usuario usuario = servicioUsuario.buscarPorCarnet(carnet);

        LocalDate hoy = LocalDate.now(ZoneId.of("America/Guatemala"));
        int rachaVigente = usuario.obtenerRacha(hoy);
        
        model.addAttribute("imagenEco", usuario.obtenerImagenEco(hoy));
        model.addAttribute("usuario", usuario);
        model.addAttribute("rachaVigente", rachaVigente);

        model.addAttribute(
            "amigosInicio",
            servicioSocial.listar(usuario.getId(), "amigos", 0)
        );

        // Usa la fecha de Guatemala que ya calculamos para la racha.
        List<Reto> retosInicio = servicioReto.obtenerRetosSemanales(hoy)
                .stream()
                .limit(3)
                .toList();

        Map<Long, ProgresoReto> progresosInicio = new HashMap<>();

        for (Reto reto : retosInicio) {
            ProgresoReto progreso = servicioReto.obtenerProgreso(
                usuario.getId(),
                reto.getId()
            );

            if (progreso != null) {
                progresosInicio.put(reto.getId(), progreso);
            }
        }

model.addAttribute("retosInicio", retosInicio);
model.addAttribute("progresosInicio", progresosInicio);
        return "inicio";
    }
}