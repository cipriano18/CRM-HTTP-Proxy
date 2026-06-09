package proxy.tls;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Extrae el SNI desde un ClientHello TLS.
 */
public class SniExtractor {

    /**
     * Intenta extraer el Server Name Indication desde los bytes
     * del primer registro TLS.
     *
     * @param tlsRecord bytes del registro TLS
     * @return dominio SNI o null si no existe
     * @throws IOException si el mensaje TLS es invalido
     */
    public String extractSni(byte[] tlsRecord) throws IOException {
        if (tlsRecord == null || tlsRecord.length < 5) {
            throw new IOException("Registro TLS incompleto");
        }

        int position = 0;

        int contentType = tlsRecord[position++] & 0xFF;
        if (contentType != 22) {
            throw new IOException("No es un registro TLS handshake");
        }

        position += 2;

        int recordLength = readUnsignedShort(tlsRecord, position);
        position += 2;

        if (tlsRecord.length < 5 + recordLength) {
            throw new IOException("Registro TLS truncado");
        }

        int handshakeType = tlsRecord[position++] & 0xFF;
        if (handshakeType != 1) {
            throw new IOException("No es un ClientHello");
        }

        int handshakeLength = readUnsignedMedium(tlsRecord, position);
        position += 3;

        if (position + handshakeLength > tlsRecord.length) {
            throw new IOException("ClientHello incompleto");
        }

        position += 2;
        position += 32;

        int sessionIdLength = tlsRecord[position++] & 0xFF;
        position += sessionIdLength;

        int cipherSuitesLength = readUnsignedShort(tlsRecord, position);
        position += 2;
        position += cipherSuitesLength;

        int compressionMethodsLength = tlsRecord[position++] & 0xFF;
        position += compressionMethodsLength;

        if (position + 2 > tlsRecord.length) {
            return null;
        }

        int extensionsLength = readUnsignedShort(tlsRecord, position);
        position += 2;

        int extensionsEnd = position + extensionsLength;

        while (position + 4 <= extensionsEnd && position + 4 <= tlsRecord.length) {
            int extensionType = readUnsignedShort(tlsRecord, position);
            position += 2;

            int extensionLength = readUnsignedShort(tlsRecord, position);
            position += 2;

            if (position + extensionLength > tlsRecord.length) {
                throw new IOException("Extension TLS invalida");
            }

            if (extensionType == 0) {
                return extractServerNameFromExtension(
                        tlsRecord,
                        position,
                        extensionLength
                );
            }

            position += extensionLength;
        }

        return null;
    }

    /**
     * Extrae el nombre de servidor desde la extension server_name.
     */
    private String extractServerNameFromExtension(
            byte[] data,
            int start,
            int length) throws IOException {

        int position = start;

        if (length < 2) {
            throw new IOException("Extension SNI invalida");
        }

        int serverNameListLength = readUnsignedShort(data, position);
        position += 2;

        int end = position + serverNameListLength;

        while (position + 3 <= end && position + 3 <= data.length) {
            int nameType = data[position++] & 0xFF;
            int nameLength = readUnsignedShort(data, position);
            position += 2;

            if (position + nameLength > data.length) {
                throw new IOException("Nombre SNI invalido");
            }

            if (nameType == 0) {
                return new String(data, position, nameLength, StandardCharsets.UTF_8)
                        .toLowerCase()
                        .trim();
            }

            position += nameLength;
        }

        return null;
    }

    /**
     * Lee un entero unsigned short desde un arreglo de bytes.
     */
    private int readUnsignedShort(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 8)
                | (data[offset + 1] & 0xFF);
    }

    /**
     * Lee un entero unsigned de 3 bytes.
     */
    private int readUnsignedMedium(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 16)
                | ((data[offset + 1] & 0xFF) << 8)
                | (data[offset + 2] & 0xFF);
    }
}
