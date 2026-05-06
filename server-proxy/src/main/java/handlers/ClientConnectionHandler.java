package handlers;

import model.ProxyRequest;

/**
 * Describe la futura atencion de conexiones de clientes al proxy.
 */
public class ClientConnectionHandler {

    /**
     * Construye un mensaje simple para describir la solicitud recibida.
     */
    public String describe(ProxyRequest request) {
        return "Handler preparado para procesar "
                + request.getMethod()
                + " hacia "
                + request.getTarget();
    }
}
