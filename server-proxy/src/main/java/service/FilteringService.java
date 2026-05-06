package service;

import java.util.Set;

/**
 * Aplica las reglas cargadas para decidir si una solicitud debe bloquearse.
 */
public class FilteringService {

    private final Set<String> blockedRules;

    /**
     * @param blockedRules reglas de dominio o palabras clave prohibidas
     */
    public FilteringService(Set<String> blockedRules) {
        this.blockedRules = blockedRules;
    }

    /**
     * Evalua una solicitud HTTP usando host y URL completos.
     */
    public boolean isHttpBlocked(String host, String url) {
        String normalizedHost = normalize(host);
        String normalizedUrl = normalize(url);

        for (String rule : blockedRules) {
            // En HTTP se permite filtrar por dominio o por fragmentos de URL.
            if (normalizedHost.contains(rule) || normalizedUrl.contains(rule)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Evalua una conexion HTTPS a partir del dominio SNI.
     */
    public boolean isHttpsBlocked(String sniHost) {
        String normalizedSni = normalize(sniHost);

        for (String rule : blockedRules) {
            if (normalizedSni.equals(rule)
                    || normalizedSni.endsWith("." + rule)) {
                return true;
            }
        }
        return false;
    }

    public int getBlockedRuleCount() {
        return blockedRules.size();
    }

    private String normalize(String value) {
        // Evita null y unifica las comparaciones en minusculas.
        return value == null ? "" : value.trim().toLowerCase();
    }
}
