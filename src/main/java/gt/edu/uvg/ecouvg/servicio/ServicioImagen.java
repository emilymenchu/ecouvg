package gt.edu.uvg.ecouvg.servicio;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ServicioImagen {

    public byte[] comprimir(MultipartFile archivo, int anchoMaximo) throws IOException {
        if (archivo == null || archivo.isEmpty()) throw new IllegalArgumentException("Imagen vacía");
        if (archivo.getSize() > 5 * 1024 * 1024) throw new IllegalArgumentException("Máximo 5 MB");
        if (anchoMaximo < 100 || anchoMaximo > 2000) throw new IllegalArgumentException("Ancho inválido");

        byte[] datos = archivo.getBytes();
        BufferedImage original;

        try (ImageInputStream entrada = ImageIO.createImageInputStream(new ByteArrayInputStream(datos))) {
            Iterator<ImageReader> lectores = ImageIO.getImageReaders(entrada);
            if (!lectores.hasNext()) throw new IllegalArgumentException("Formato no compatible");

            ImageReader lector = lectores.next();
            try {
                lector.setInput(entrada);
                String formato = lector.getFormatName().toLowerCase();
                if (!formato.equals("jpeg") && !formato.equals("jpg") && !formato.equals("png") && !formato.equals("webp"))
                    throw new IllegalArgumentException("Formato no permitido");

                long pixeles = (long) lector.getWidth(0) * lector.getHeight(0);
                if (pixeles > 20_000_000L) throw new IllegalArgumentException("Imagen demasiado grande");
                original = lector.read(0);
            } finally {
                lector.dispose();
            }
        }

        double escala = Math.min(1.0, (double) anchoMaximo / original.getWidth());
        int ancho = Math.max(1, (int) Math.round(original.getWidth() * escala));
        int alto = Math.max(1, (int) Math.round(original.getHeight() * escala));

        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagen.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, ancho, alto);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(original, 0, 0, ancho, alto, null);
        } finally {
            g.dispose();
        }

        Iterator<ImageWriter> escritores = ImageIO.getImageWritersByFormatName("jpeg");
        if (!escritores.hasNext()) throw new IOException("Codificador JPEG no disponible");

        ImageWriter escritor = escritores.next();
        ByteArrayOutputStream salida = new ByteArrayOutputStream();

        try (ImageOutputStream flujo = ImageIO.createImageOutputStream(salida)) {
            escritor.setOutput(flujo);
            ImageWriteParam parametros = escritor.getDefaultWriteParam();
            parametros.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            parametros.setCompressionQuality(0.8f);
            escritor.write(null, new IIOImage(imagen, null, null), parametros);
        } finally {
            escritor.dispose();
        }

        return salida.toByteArray();
    }
}