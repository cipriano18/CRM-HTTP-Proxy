package model;

/**
 * Modelo basico de una solicitud que pasa por el proxy.
 */
public class ProxyRequest {

    private final String method;
    private final String target;
    private final String clientIpAddress;

    /**
     * @param method metodo HTTP recibido
     * @param target destino o recurso solicitado
     * @param clientIpAddress direccion IP del cliente
     */
    public ProxyRequest(
            String method,
            String target,
            String clientIpAddress) {
        this.method = method;
        this.target = target;
        this.clientIpAddress = clientIpAddress;
    }

    public String getMethod() {
        return method;
    }

    public String getTarget() {
        return target;
    }

    public String getClientIpAddress() {
        return clientIpAddress;
    }
}
