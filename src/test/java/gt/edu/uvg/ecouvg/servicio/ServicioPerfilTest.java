package gt.edu.uvg.ecouvg.servicio;

import java.io.IOException;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.repositorio.RepositorioUsuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServicioPerfilTest {
    private RepositorioUsuario usuarios;
    private ServicioStorage storage;
    private ServicioPerfil perfil;
    private Usuario usuario;
    private AnnotationConfigApplicationContext contexto;
    private TransactionTemplate transaccion;

    @Configuration
    @EnableTransactionManagement
    static class ConfiguracionTransacciones {}
    private final MockMultipartFile fotografia = new MockMultipartFile("fotografia", "foto.png", "image/png", new byte[]{1});

    @BeforeEach void preparar() {
        usuarios = mock(RepositorioUsuario.class);
        storage = mock(ServicioStorage.class);
        contexto = new AnnotationConfigApplicationContext();
        contexto.register(ConfiguracionTransacciones.class);
        contexto.registerBean(RepositorioUsuario.class, () -> usuarios);
        contexto.registerBean(ServicioStorage.class, () -> storage);
        contexto.registerBean(PlatformTransactionManager.class, () -> new DataSourceTransactionManager(
            new DriverManagerDataSource("jdbc:h2:mem:perfil_test;DB_CLOSE_DELAY=-1", "sa", "")));
        contexto.registerBean(PasswordEncoder.class, () -> new BCryptPasswordEncoder(4));
        contexto.registerBean(ServicioPerfil.class);
        contexto.refresh();
        perfil = contexto.getBean(ServicioPerfil.class);
        transaccion = new TransactionTemplate(contexto.getBean(PlatformTransactionManager.class));
        usuario = new Usuario("Pilar", "123", "pilar@example.com", "clave");
        ReflectionTestUtils.setField(usuario, "id", 1L);
        usuario.setFotoPerfil("usuario_1/anterior.jpg");
        when(usuarios.findByCarnet("123")).thenReturn(Optional.of(usuario));
    }

    @AfterEach void limpiar() {
        contexto.close();
    }

    @Test void cambiarFotoConservaLosDatosDeCuenta() throws Exception {
        when(storage.subirImagen(fotografia, "perfiles", "usuario_1")).thenReturn("usuario_1/nueva.jpg");
        perfil.actualizarFoto("123", fotografia);
        assertEquals("Pilar", usuario.getNombre());
        assertEquals("123", usuario.getCarnet());
        assertEquals("pilar@example.com", usuario.getCorreo());
        assertEquals(0, usuario.getPuntos());
        verify(usuarios).saveAndFlush(usuario);
    }

    @Test void fotoAnteriorSeEliminaSoloDespuesDelCommit() throws Exception {
        when(storage.subirImagen(fotografia, "perfiles", "usuario_1")).thenReturn("usuario_1/nueva.jpg");
        transaccion.executeWithoutResult(estado -> {
            assertDoesNotThrow(() -> perfil.actualizarFoto("123", fotografia));
            assertEquals("usuario_1/nueva.jpg", usuario.getFotoPerfil());
            verify(storage, never()).eliminarImagen(anyString(), anyString());
        });
        verify(storage).eliminarImagen("perfiles", "usuario_1/anterior.jpg");
        verify(storage, never()).eliminarImagen("perfiles", "usuario_1/nueva.jpg");
    }

    @Test void falloAlGuardarDescartaFotoNuevaYConservaLaAnterior() throws Exception {
        when(storage.subirImagen(fotografia, "perfiles", "usuario_1")).thenReturn("usuario_1/nueva.jpg");
        doThrow(new IllegalStateException("fallo BD")).when(usuarios).saveAndFlush(usuario);
        assertThrows(IllegalStateException.class, () -> perfil.actualizarFoto("123", fotografia));
        verify(storage).eliminarImagen("perfiles", "usuario_1/nueva.jpg");
        verify(storage, never()).eliminarImagen("perfiles", "usuario_1/anterior.jpg");
    }

    @Test void sinArchivoNoGuarda() {
        assertThrows(IllegalArgumentException.class, () -> perfil.actualizarFoto("123", null));
        assertThrows(IllegalArgumentException.class, () -> perfil.actualizarFoto("123",
            new MockMultipartFile("fotografia", new byte[0])));
        verifyNoInteractions(storage, usuarios);
    }

    @Test void falloEnStorageNoModificaElUsuario() throws Exception {
        when(storage.subirImagen(fotografia, "perfiles", "usuario_1")).thenThrow(new IOException("fallo de imagen"));
        assertThrows(IOException.class, () -> perfil.actualizarFoto("123", fotografia));
        assertEquals("Pilar", usuario.getNombre());
        assertEquals("usuario_1/anterior.jpg", usuario.getFotoPerfil());
        verify(usuarios, never()).saveAndFlush(any());
    }

    @Test void falloDeLimpiezaNoInvalidaUnGuardadoConfirmado() throws Exception {
        when(storage.subirImagen(fotografia, "perfiles", "usuario_1")).thenReturn("usuario_1/nueva.jpg");
        doThrow(new IllegalStateException("storage no disponible")).when(storage).eliminarImagen(anyString(), anyString());
        assertDoesNotThrow(() -> perfil.actualizarFoto("123", fotografia));
        assertEquals("usuario_1/nueva.jpg", usuario.getFotoPerfil());
    }

    @Test void sinFotoDevuelveAvatarPredeterminado() {
        usuario.setFotoPerfil(null);
        assertNull(perfil.obtenerFoto("123"));
        verifyNoInteractions(storage);
    }
    @Test void obtieneFotoMedianteStorageAutenticado() {
        byte[] imagen = new byte[]{1, 2, 3};
        when(storage.obtenerImagen("perfiles", "usuario_1/anterior.jpg")).thenReturn(imagen);
        assertArrayEquals(imagen, perfil.obtenerFoto("123"));
        verify(storage, never()).obtenerUrlPublica(anyString(), anyString());
    }

    @Test void cambiaContrasenaSoloAlValidarLaActualYGuardaHash() {
        PasswordEncoder encoder = contexto.getBean(PasswordEncoder.class);
        usuario.setContrasena(encoder.encode("actual123"));
        perfil.cambiarContrasena("123", "actual123", "NuevaClave123", "NuevaClave123");
        assertTrue(encoder.matches("NuevaClave123", usuario.getContrasena()));
        assertNotEquals("NuevaClave123", usuario.getContrasena());
        verify(usuarios).saveAndFlush(usuario);
    }

    @Test void rechazaContrasenaActualIncorrecta() {
        PasswordEncoder encoder = contexto.getBean(PasswordEncoder.class);
        usuario.setContrasena(encoder.encode("actual123"));
        assertThrows(IllegalArgumentException.class,
            () -> perfil.cambiarContrasena("123", "incorrecta", "NuevaClave123", "NuevaClave123"));
        assertTrue(encoder.matches("actual123", usuario.getContrasena()));
        verify(usuarios, never()).saveAndFlush(any());
    }

    @Test void rechazaConfirmacionDistintaCortaYExcesoDeBytes() {
        PasswordEncoder encoder = contexto.getBean(PasswordEncoder.class);
        usuario.setContrasena(encoder.encode("actual123"));
        assertThrows(IllegalArgumentException.class,
            () -> perfil.cambiarContrasena("123", "actual123", "NuevaClave123", "OtraClave123"));
        assertThrows(IllegalArgumentException.class,
            () -> perfil.cambiarContrasena("123", "actual123", "corta", "corta"));
        String larga = "á".repeat(37);
        assertThrows(IllegalArgumentException.class,
            () -> perfil.cambiarContrasena("123", "actual123", larga, larga));
        verify(usuarios, never()).saveAndFlush(any());
    }

}
