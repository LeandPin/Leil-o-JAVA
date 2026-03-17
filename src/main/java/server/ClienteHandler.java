package server;

import java.io.*;
import java.net.Socket;

public class ClienteHandler implements Runnable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private int salaAtual = -1;
    private String nomeUsuario = "Anônimo";

    public ClienteHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in  = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            ClientManager.add(this);

            // Aguarda GUI renderizar, depois envia o catálogo completo
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    for (var entry : AuctionServer.salas.entrySet()) {
                        var s = entry.getValue();
                        var item = s.getItem();
                        // ITEM <id> <precoInicial> <imagemPath> <nome com espaços>
                        enviar("ITEM " + item.getId()
                                + " " + item.getPrecoInicial()
                                + " " + item.getImagemPath()
                                + " " + item.getNome());
                        enviar("NOVO_VALOR " + item.getId() + " " + s.getMaiorLance().getValor());
                        enviar("TEMPO "      + item.getId() + " " + s.getTempoRestante());
                    }
                } catch (InterruptedException ignored) {}
            }).start();

            while (true) {
                String mensagem = in.readUTF();
                if (mensagem.equalsIgnoreCase("SAIR")) break;

                if (mensagem.toUpperCase().startsWith("LANCE")) {
                    String[] p = mensagem.split(" ");
                    if (p.length >= 2) nomeUsuario = p[1];
                }

                String resposta = Protocol.processLine(mensagem, this);
                if (!resposta.isEmpty()) enviar(resposta);
            }
        } catch (IOException e) {
            System.out.println("Cliente " + nomeUsuario + " desconectado.");
        } finally {
            fecharConexao();
        }
    }

    public void enviar(String mensagem) {
        try {
            if (out != null) { out.writeUTF(mensagem); out.flush(); }
        } catch (IOException ignored) {}
    }

    private void fecharConexao() {
        try {
            ClientManager.remove(this);
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public String getNomeUsuario()          { return nomeUsuario; }
    public int    getSalaAtual()            { return salaAtual; }
    public void   setSalaAtual(int id)      { this.salaAtual = id; }
    public Socket getSocket()               { return socket; }
}