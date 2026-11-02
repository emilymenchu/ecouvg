package gt.edu.uvg.ecouvg.servicio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Lee solo Orientation del IFD0. Un EXIF ausente o inválido equivale a orientación normal. */
final class OrientacionExif {
    private OrientacionExif() {}

    static int leer(byte[] datos) {
        if (datos.length >= 2 && (datos[0] & 255) == 0xff && (datos[1] & 255) == 0xd8) {
            int posicion = 2;
            while (posicion + 4 <= datos.length && (datos[posicion] & 255) == 0xff) {
                while (posicion < datos.length && (datos[posicion] & 255) == 0xff) posicion++;
                if (posicion >= datos.length) break;
                int marcador = datos[posicion++] & 255;
                if (marcador == 0xda || marcador == 0xd9) break;
                if (marcador == 0x01 || marcador >= 0xd0 && marcador <= 0xd8) continue;
                if (posicion + 2 > datos.length) break;
                int longitud = (datos[posicion] & 255) << 8 | datos[posicion + 1] & 255;
                if (longitud < 2 || posicion + longitud > datos.length) break;
                if (marcador == 0xe1 && longitud >= 8
                        && datos[posicion + 2] == 'E' && datos[posicion + 3] == 'x'
                        && datos[posicion + 4] == 'i' && datos[posicion + 5] == 'f'
                        && datos[posicion + 6] == 0 && datos[posicion + 7] == 0) {
                    return leerTiff(datos, posicion + 8, longitud - 8);
                }
                posicion += longitud;
            }
        } else if (datos.length >= 8 && ByteBuffer.wrap(datos).getLong() == 0x89504e470d0a1a0aL) {
            // PNG guarda el mismo TIFF en el chunk eXIf, sin el prefijo Exif\0\0.
            ByteBuffer png = ByteBuffer.wrap(datos);
            int posicion = 8;
            while (posicion + 12 <= datos.length) {
                int longitud = png.getInt(posicion);
                if (longitud < 0 || (long) posicion + 12 + longitud > datos.length) break;
                if (png.getInt(posicion + 4) == 0x65584966) {
                    return leerTiff(datos, posicion + 8, longitud);
                }
                posicion += 12 + longitud;
            }
        }
        return 1;
    }

    private static int leerTiff(byte[] datos, int inicio, int longitud) {
        if (longitud < 8) return 1;
        ByteBuffer tiff = ByteBuffer.wrap(datos, inicio, longitud).slice();
        int orden = tiff.getShort(0) & 0xffff;
        if (orden == 0x4949) tiff.order(ByteOrder.LITTLE_ENDIAN);
        else if (orden != 0x4d4d) return 1;
        if (tiff.getShort(2) != 42) return 1;
        long desplazamiento = Integer.toUnsignedLong(tiff.getInt(4));
        if (desplazamiento < 8 || desplazamiento > longitud - 2) return 1;
        int directorio = (int) desplazamiento;
        int entradas = tiff.getShort(directorio) & 0xffff;
        for (int i = 0; i < entradas; i++) {
            long posicion = (long) directorio + 2 + i * 12L;
            if (posicion + 12 > longitud) return 1;
            int entrada = (int) posicion;
            if ((tiff.getShort(entrada) & 0xffff) == 0x0112) {
                if (tiff.getShort(entrada + 2) != 3 || tiff.getInt(entrada + 4) != 1) return 1;
                int orientacion = tiff.getShort(entrada + 8) & 0xffff;
                return orientacion >= 1 && orientacion <= 8 ? orientacion : 1;
            }
        }
        return 1;
    }
}
