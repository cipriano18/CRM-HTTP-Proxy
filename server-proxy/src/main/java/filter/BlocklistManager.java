/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cipriano
 */
public class BlocklistManager {

    /**
     * Ruta del archivo donde se guardan las reglas.
     */
    private static final String FILE_PATH =
            "src/main/resources/data/blocklist.txt";

    /**
     * Obtiene todas las reglas del archivo.
     *
     * @return lista de reglas DOMAIN o KEYWORD.
     */
    public static List<String> getRules() {
        try {
            return Files.readAllLines(Path.of(FILE_PATH));
        } catch (IOException e) {
            System.out.println("Error leyendo blocklist.txt");
            return new ArrayList<>();
        }
    }

    /**
     * Agrega un dominio bloqueado.
     *
     * Ejemplo:
     * DOMAIN:youtube.com
     *
     * @param domain dominio a bloquear.
     */
    public static void addDomain(String domain) {
        addRule("DOMAIN", domain);
    }

    /**
     * Agrega una palabra clave bloqueada.
     *
     * Ejemplo:
     * KEYWORD:juegos
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
        try {
            String rule = type.toUpperCase() + ":" + value.trim().toLowerCase();

            List<String> rules = getRules();

            if (rules.contains(rule)) {
                System.out.println("La regla ya existe: " + rule);
                return;
            }

            Files.write(
                    Path.of(FILE_PATH),
                    (rule + System.lineSeparator()).getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            System.out.println("Regla agregada: " + rule);

        } catch (IOException e) {
            System.out.println("Error agregando regla");
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
            rule = rule.trim().toLowerCase();

            if (rule.startsWith("domain:")) {
                domains.add(rule.replace("domain:", "").trim());
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
            rule = rule.trim().toLowerCase();

            if (rule.startsWith("keyword:")) {
                keywords.add(rule.replace("keyword:", "").trim());
            }
        }

        return keywords;
    }
}