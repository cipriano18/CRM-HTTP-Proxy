package filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Administra las reglas de bloqueo almacenadas en blocklist.txt.
 */
public class BlocklistManager {

    /**
     * Ruta del archivo donde se guardan las reglas.
     */
    private static final Path FILE_PATH =
            Path.of("src/main/resources/data/blocklist.txt");

    /**
     * Obtiene todas las reglas del archivo.
     *
     * @return lista de reglas DOMAIN o KEYWORD.
     */
    public static List<String> getRules() {
        try {
            ensureFileExists();
            String content = Files.readString(
                    FILE_PATH,
                    StandardCharsets.UTF_8
            );
            return parseRules(content);
        } catch (IOException e) {
            System.out.println("Error leyendo blocklist.txt");
            return new ArrayList<>();
        }
    }

    /**
     * Agrega un dominio bloqueado.
     *
     * @param domain dominio a bloquear.
     */
    public static void addDomain(String domain) {
        addRule("DOMAIN", domain);
    }

    /**
     * Elimina un dominio bloqueado.
     *
     * @param domain dominio a eliminar.
     * @return true si el dominio existia y fue eliminado.
     */
    public static boolean removeDomain(String domain) {
        return removeRule("DOMAIN", domain);
    }

    /**
     * Elimina una palabra clave bloqueada.
     *
     * @param keyword palabra clave a eliminar.
     * @return true si la palabra existia y fue eliminada.
     */
    public static boolean removeKeyword(String keyword) {
        return removeRule("KEYWORD", keyword);
    }

    /**
     * Agrega una palabra clave bloqueada.
     *
     * @param keyword palabra clave a bloquear.
     */
    public static void addKeyword(String keyword) {
        addRule("KEYWORD", keyword);
    }

    /**
     * Agrega una regla al archivo blocklist.txt.
     *
     * @param type DOMAIN o KEYWORD.
     * @param value valor a bloquear.
     */
    private static void addRule(String type, String value) {
        String normalizedValue = normalizeValue(value);

        if (normalizedValue.isBlank()) {
            return;
        }

        try {
            ensureFileExists();

            String rule = buildRule(type, normalizedValue);
            List<String> rules = getRules();

            if (rules.stream().anyMatch(existingRule
                    -> existingRule.trim().equalsIgnoreCase(rule))) {
                System.out.println("La regla ya existe: " + rule);
                return;
            }

            Files.write(
                    FILE_PATH,
                    (rule + System.lineSeparator())
                            .getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            System.out.println("Regla agregada: " + rule);

        } catch (IOException e) {
            System.out.println("Error agregando regla");
        }
    }

    /**
     * Elimina una regla de la blocklist.
     *
     * @param type DOMAIN o KEYWORD.
     * @param value valor a eliminar.
     * @return true si la regla existia y fue eliminada.
     */
    private static boolean removeRule(String type, String value) {
        String normalizedValue = normalizeValue(value);
        String expectedRule = buildRule(type, normalizedValue);

        try {
            ensureFileExists();

            List<String> updatedRules = new ArrayList<>();
            boolean removed = false;

            for (String rule : getRules()) {
                if (!removed
                        && rule.trim().equalsIgnoreCase(expectedRule)) {
                    removed = true;
                    continue;
                }

                updatedRules.add(rule);
            }

            if (!removed) {
                return false;
            }

            Files.write(
                    FILE_PATH,
                    updatedRules,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            return true;

        } catch (IOException e) {
            System.out.println("Error eliminando regla");
            return false;
        }
    }

    /**
     * Obtiene solamente los dominios bloqueados.
     *
     * @return lista de dominios.
     */
    public static List<String> getDomains() {
        List<String> domains = new ArrayList<>();

        for (String rule : getRules()) {
            String normalizedRule = rule.trim().toLowerCase();

            if (normalizedRule.startsWith("domain:")) {
                domains.add(
                        normalizedRule.replace("domain:", "").trim()
                );
            }
        }

        return domains;
    }

    /**
     * Obtiene solamente las palabras clave bloqueadas.
     *
     * @return lista de palabras clave.
     */
    public static List<String> getKeywords() {
        List<String> keywords = new ArrayList<>();

        for (String rule : getRules()) {
            String normalizedRule = rule.trim().toLowerCase();

            if (normalizedRule.startsWith("keyword:")) {
                keywords.add(
                        normalizedRule.replace("keyword:", "").trim()
                );
            }
        }

        return keywords;
    }

    /**
     * Crea el archivo de reglas si todavia no existe.
     *
     * @throws IOException si no se puede crear.
     */
    private static void ensureFileExists() throws IOException {
        if (Files.notExists(FILE_PATH.getParent())) {
            Files.createDirectories(FILE_PATH.getParent());
        }

        if (Files.notExists(FILE_PATH)) {
            Files.createFile(FILE_PATH);
        }
    }

    /**
     * Construye el formato estandar de una regla.
     *
     * @param type tipo de regla.
     * @param value valor limpio.
     * @return regla formateada.
     */
    private static String buildRule(String type, String value) {
        return type.toUpperCase() + ":" + value;
    }

    /**
     * Parsea reglas incluso si quedaron concatenadas en una sola linea.
     *
     * @param content contenido bruto del archivo.
     * @return lista de reglas individuales.
     */
    private static List<String> parseRules(String content) {
        List<String> rules = new ArrayList<>();

        if (content == null || content.isBlank()) {
            return rules;
        }

        String normalizedContent = content
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("(?i)(?<!^)(DOMAIN:|KEYWORD:)", "\n$1");

        for (String line : normalizedContent.split("\\n")) {
            String normalizedLine = line.trim();

            if (normalizedLine.isBlank()) {
                continue;
            }

            int separatorIndex = normalizedLine.indexOf(':');
            if (separatorIndex <= 0) {
                continue;
            }

            String type = normalizedLine.substring(
                    0,
                    separatorIndex
            ).trim().toUpperCase();

            String value = normalizeValue(
                    normalizedLine.substring(separatorIndex + 1)
            );

            if (("DOMAIN".equals(type) || "KEYWORD".equals(type))
                    && !value.isBlank()) {
                rules.add(buildRule(type, value));
            }
        }

        return rules;
    }

    /**
     * Normaliza un valor antes de persistirlo.
     *
     * @param value valor original.
     * @return valor limpio en minusculas.
     */
    private static String normalizeValue(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().toLowerCase();
    }
}
