package server;

import model.Lance;
import model.Sala;

public class Protocol {
    public static String processLine(String input, ClienteHandler handler) {
        if (input == null || input.isEmpty()) return "";

        String[] partes = input.split(" ", 3);
        String comando = partes[0].toUpperCase();

        // ENTRAR <idSala>
        if (comando.equals("ENTRAR")) {
            try {
                int id = Integer.parseInt(partes[1]);
                Sala s = AuctionServer.salas.get(id);
                if (s != null) {
                    handler.setSalaAtual(id);
                    String status = s.isFinalizado() ? "FINALIZADO" : "ATIVO";
                    return "INFO " + id + " " + s.getItem().getNome()
                            + " " + status + " " + s.getMaiorLance().getValor();
                }
                return "ERRO Sala não encontrada.";
            } catch (Exception e) {
                return "ERRO ID inválido.";
            }
        }

        // LANCE <nome> <valor>
        if (comando.equals("LANCE")) {
            if (handler.getSalaAtual() == -1) return "ERRO Selecione um produto primeiro.";

            try {
                String nome = partes[1];
                double valor = Double.parseDouble(partes[2]);
                Sala s = AuctionServer.salas.get(handler.getSalaAtual());

                if (s == null || s.isFinalizado()) return "ERRO Leilão finalizado.";

                synchronized (Protocol.class) {
                    if (s.registrarLance(new Lance(valor, nome))) {
                        int salaId = handler.getSalaAtual();
                        // Atualiza preço do card para todos
                        ClientManager.broadcastAll("NOVO_VALOR " + salaId + " " + valor);
                        // Reseta timer visualmente
                        ClientManager.broadcastAll("TEMPO " + salaId + " 60");
                        // Notificação no chat lateral
                        ClientManager.broadcastAll("MSG " + salaId + " 📢 " + nome + " deu R$" + valor + " em " + s.getItem().getNome());
                        return "SUCESSO Lance registrado!";
                    } else {
                        return "NEGADO Lance abaixo do atual ou leilão encerrado.";
                    }  
                }
            } catch (Exception e) {
                return "ERRO Formato: LANCE <nome> <valor>";
            }
        }

        return "ERRO Comando não reconhecido.";
    }
}