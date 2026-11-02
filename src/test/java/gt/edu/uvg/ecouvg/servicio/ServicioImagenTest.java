package gt.edu.uvg.ecouvg.servicio;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import static org.junit.jupiter.api.Assertions.*;

class ServicioImagenTest {
    private final ServicioImagen servicio = new ServicioImagen();
    private static final Color[] COLORES = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW};

    @Test void normalizaLasOchoOrientacionesEnAmbosOrdenesDeBytes() throws Exception {
        int[][] esperadas = {
            {0, 1, 2, 3}, {1, 0, 3, 2}, {3, 2, 1, 0}, {2, 3, 0, 1},
            {0, 2, 1, 3}, {2, 0, 3, 1}, {3, 1, 2, 0}, {1, 3, 0, 2}
        };
        for (ByteOrder orden : new ByteOrder[]{ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN}) {
            for (int orientacion = 1; orientacion <= 8; orientacion++) {
                byte[] jpeg = conExifJpeg(imagen("jpg", 120, 80), tiff(orientacion, orden));
                BufferedImage salida = comprimir(jpeg, 400);
                assertEquals(orientacion >= 5 ? 80 : 120, salida.getWidth());
                assertEquals(orientacion >= 5 ? 120 : 80, salida.getHeight());
                verificarEsquinas(salida, esperadas[orientacion - 1]);
            }
        }
    }

    @Test void escalaDespuesDeRotarYNoVuelveAGirarUnaImagenProcesada() throws Exception {
        byte[] original = conExifJpeg(imagen("jpg", 240, 160), tiff(6, ByteOrder.LITTLE_ENDIAN));
        byte[] salida = servicio.comprimir(new MockMultipartFile("foto", original), 100);
        BufferedImage imagen = ImageIO.read(new ByteArrayInputStream(salida));
        assertEquals(100, imagen.getWidth());
        assertEquals(150, imagen.getHeight());
        verificarEsquinas(imagen, new int[]{2, 0, 3, 1});
        assertEquals(1, OrientacionExif.leer(salida));
        verificarEsquinas(comprimir(salida, 400), new int[]{2, 0, 3, 1});
    }

    @Test void fotoSinExifConservaSuOrientacion() throws Exception {
        verificarEsquinas(comprimir(imagen("jpg", 120, 80), 400), new int[]{0, 1, 2, 3});
    }

    @Test void tambienAplicaExifDePng() throws Exception {
        byte[] png = imagen("png", 120, 80);
        byte[] metadatos = tiff(3, ByteOrder.BIG_ENDIAN);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        salida.write(png, 0, 33); // Firma y chunk IHDR.
        salida.write(ByteBuffer.allocate(4).putInt(metadatos.length).array());
        byte[] tipo = {'e', 'X', 'I', 'f'};
        salida.write(tipo);
        salida.write(metadatos);
        CRC32 crc = new CRC32();
        crc.update(tipo);
        crc.update(metadatos);
        salida.write(ByteBuffer.allocate(4).putInt((int) crc.getValue()).array());
        salida.write(png, 33, png.length - 33);
        verificarEsquinas(comprimir(salida.toByteArray(), 400), new int[]{3, 2, 1, 0});
    }

    @Test void metadatosTruncadosOConOffsetsInvalidosNoRompenLaLectura() throws Exception {
        byte[] base = imagen("jpg", 120, 80);
        for (int longitud = 0; longitud < 22; longitud++) {
            byte[] truncado = java.util.Arrays.copyOf(tiff(3, ByteOrder.LITTLE_ENDIAN), longitud);
            assertEquals(1, OrientacionExif.leer(conExifJpeg(base, truncado)));
        }
        byte[] invalido = tiff(3, ByteOrder.LITTLE_ENDIAN);
        ByteBuffer.wrap(invalido).order(ByteOrder.LITTLE_ENDIAN).putInt(4, -1);
        assertEquals(1, OrientacionExif.leer(conExifJpeg(base, invalido)));
        assertEquals(1, OrientacionExif.leer(conExifJpeg(base, tiff(99, ByteOrder.BIG_ENDIAN))));
    }

    private BufferedImage comprimir(byte[] datos, int ancho) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(servicio.comprimir(new MockMultipartFile("foto", datos), ancho)));
    }

    private byte[] imagen(String formato, int ancho, int alto) throws Exception {
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                imagen.setRGB(x, y, COLORES[(y < alto / 2 ? 0 : 2) + (x < ancho / 2 ? 0 : 1)].getRGB());
            }
        }
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(imagen, formato, salida);
        return salida.toByteArray();
    }

    private byte[] tiff(int orientacion, ByteOrder orden) {
        ByteBuffer datos = ByteBuffer.allocate(26).order(orden);
        datos.putShort((short) (orden == ByteOrder.LITTLE_ENDIAN ? 0x4949 : 0x4d4d));
        datos.putShort((short) 42).putInt(8).putShort((short) 1);
        datos.putShort((short) 0x0112).putShort((short) 3).putInt(1).putShort((short) orientacion).putShort((short) 0).putInt(0);
        return datos.array();
    }

    private byte[] conExifJpeg(byte[] jpeg, byte[] tiff) throws Exception {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        salida.write(jpeg, 0, 2);
        salida.write(new byte[]{(byte) 0xff, (byte) 0xe1});
        salida.write(ByteBuffer.allocate(2).putShort((short) (8 + tiff.length)).array());
        salida.write(new byte[]{'E', 'x', 'i', 'f', 0, 0});
        salida.write(tiff);
        salida.write(jpeg, 2, jpeg.length - 2);
        return salida.toByteArray();
    }

    private void verificarEsquinas(BufferedImage imagen, int[] esperadas) {
        int[][] posiciones = {{10, 10}, {imagen.getWidth() - 11, 10}, {10, imagen.getHeight() - 11}, {imagen.getWidth() - 11, imagen.getHeight() - 11}};
        for (int i = 0; i < posiciones.length; i++) {
            Color real = new Color(imagen.getRGB(posiciones[i][0], posiciones[i][1]));
            Color esperado = COLORES[esperadas[i]];
            assertTrue(Math.abs(real.getRed() - esperado.getRed()) < 30
                && Math.abs(real.getGreen() - esperado.getGreen()) < 30
                && Math.abs(real.getBlue() - esperado.getBlue()) < 30, "Esquina incorrecta: " + i);
        }
    }
}
