package service;

import java.util.Set;
import repository.BlocklistRepository;
import server.ProxyServer;

/**
 * Servicio coordinador para inicializar dependencias del proxy.
 */
public class ProxyRuntimeService {

    private final ProxyServer proxyServer;
    private final BlocklistRepository blocklistRepository;
    private final FilteringService filteringService;

    public ProxyRuntimeService(
            ProxyServer proxyServer,
            BlocklistRepository blocklistRepository) {
        this.proxyServer = proxyServer;
        this.blocklistRepository = blocklistRepository;
        Set<String> blockedRules = blocklistRepository.loadBlockedDomains();
        this.filteringService = new FilteringService(blockedRules);
    }

    /**
     * Genera un mensaje simple con el estado inicial del proxy.
     */
    public String getStartupMessage() {
        return proxyServer.getStatus()
                + " Reglas bloqueadas cargadas: "
                + filteringService.getBlockedRuleCount()
                + ".";
    }

    /**
     * Delega la validacion de bloqueo para trafico HTTP.
     */
    public boolean isHttpBlocked(String host, String url) {
        return filteringService.isHttpBlocked(host, url);
    }

    /**
     * Delega la validacion de bloqueo para trafico HTTPS por SNI.
     */
    public boolean isHttpsBlocked(String sniHost) {
        return filteringService.isHttpsBlocked(sniHost);
    }
}
