package repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Lee y normaliza las reglas de bloqueo desde un archivo de recursos.
 */
public class BlocklistRepository {

    private static final String BLOCKLIST_RESOURCE = "/blocklist.txt";

    /**
     * Carga las reglas bloqueadas desde el archivo de recursos.
     *
     * @return conjunto inmutable de reglas normalizadas
     */
    public Set<String> loadBlockedDomains() {
        InputStream inputStream = BlocklistRepository.class
                .getResourceAsStream(BLOCKLIST_RESOURCE);
        if (inputStream == null) {
            System.err.println(
                    "No se encontro el archivo de blocklist: "
                    + BLOCKLIST_RESOURCE);
            return Set.of();
        }

        Set<String> blockedRules = new LinkedHashSet<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String normalized = line.trim().toLowerCase();
                // Se ignoran comentarios y lineas vacias del archivo.
                if (normalized.isEmpty() || normalized.startsWith("#")) {
                    continue;
                }
                blockedRules.add(normalized);
            }
        } catch (IOException exception) {
            System.err.println(
                    "No fue posible leer el archivo de blocklist: "
                    + exception.getMessage());
            return Set.of();
        }

        return Set.copyOf(blockedRules);
    }
}
