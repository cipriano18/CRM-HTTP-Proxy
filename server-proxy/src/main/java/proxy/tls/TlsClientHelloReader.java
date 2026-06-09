/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proxy.tls;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Lee del cliente los primeros bytes del saludo TLS
 * hasta completar al menos un registro TLS.
 */
public class TlsClientHelloReader {
    private static final int TLS_HEADER_LENGTH = 5;
    private static final int MAX_TLS_RECORD_SIZE = 18432;

    /**
     * Lee un registro TLS completo desde el cliente.
     *
     * @param input flujo de entrada del cliente
     * @return bytes completos del primer registro TLS
     * @throws IOException si el flujo termina o el mensaje es invalido
     */
    public byte[] readClientHello(InputStream input) throws IOException {
        byte[] header = readExactBytes(input, TLS_HEADER_LENGTH);

        int contentType = header[0] & 0xFF;
        if (contentType != 22) {
            throw new IOException("El primer registro no es un handshake TLS");
        }

        int recordLength =
                ((header[3] & 0xFF) << 8)
                | (header[4] & 0xFF);

        if (recordLength <= 0 || recordLength > MAX_TLS_RECORD_SIZE) {
            throw new IOException("Longitud TLS invalida: " + recordLength);
        }

        byte[] body = readExactBytes(input, recordLength);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(header);
        output.write(body);

        return output.toByteArray();
    }

    /**
     * Lee exactamente la cantidad de bytes solicitada.
     *
     * @param input flujo origen
     * @param length cantidad de bytes esperada
     * @return arreglo con los bytes leidos
     * @throws IOException si no se pueden leer todos los bytes
     */
    private byte[] readExactBytes(InputStream input, int length) throws IOException {
        byte[] buffer = new byte[length];
        int totalRead = 0;

        while (totalRead < length) {
            int bytesRead = input.read(buffer, totalRead, length - totalRead);

            if (bytesRead == -1) {
                throw new IOException("Conexion cerrada antes de completar lectura TLS");
            }

            totalRead += bytesRead;
        }

        return buffer;
    }
}
