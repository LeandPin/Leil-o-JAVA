package server;

import java.util.ArrayList;
import java.util.List;

public class ClientManager {
    private static List<ClienteHandler> clientes = new ArrayList<>();

    public static synchronized void add(ClienteHandler cliente) {
        clientes.add(cliente);
    }

    public static synchronized void remove(ClienteHandler cliente) {
        clientes.remove(cliente);
    }

    // Envia para TODOS os clientes conectados (independente de sala)
    public static synchronized void broadcastAll(String mensagem) {
        for (ClienteHandler cliente : clientes) {
            cliente.enviar(mensagem);
        }
    }

    public static int count() {
        return clientes.size();
    }
}