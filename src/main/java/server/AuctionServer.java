package server;

import model.Item;
import model.Sala;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.concurrent.*;

public class AuctionServer {
    public static ConcurrentHashMap<Integer, Sala> salas = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        carregarItens("items.json");

        if (salas.isEmpty()) {
            System.out.println("⚠️  Nenhum item carregado! Verifique o items.json.");
            return;
        }

        try (ServerSocket server = new ServerSocket(12345)) {
            System.out.println("✅ Servidor de Leilão Online na porta 12345");
            System.out.println("📦 Salas abertas: " + salas.size());
            ExecutorService pool = Executors.newCachedThreadPool();
            while (true) {
                Socket s = server.accept();
                pool.execute(new ClienteHandler(s));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Lê o items.json sem biblioteca externa — parsing manual do JSON
    private static void carregarItens(String caminhoJson) {
        try {
            String json = Files.readString(Path.of(caminhoJson));
            // Remove espaços e quebras desnecessárias
            json = json.trim();
            // Cada objeto fica entre { }
            String[] objetos = json.replaceAll("\\[|\\]", "").split("\\},\\s*\\{");

            for (String obj : objetos) {
                obj = obj.replaceAll("[\\{\\}]", "").trim();
                int    id           = 0;
                String nome         = "";
                double precoInicial = 0.0;
                String imagem       = "";

                for (String linha : obj.split(",")) {
                    String[] kv = linha.split(":", 2);
                    if (kv.length < 2) continue;
                    String chave = kv[0].replaceAll("\"", "").trim();
                    String valor = kv[1].replaceAll("\"", "").trim();

                    switch (chave) {
                        case "id"           -> id           = Integer.parseInt(valor);
                        case "nome"         -> nome         = valor;
                        case "precoInicial" -> precoInicial = Double.parseDouble(valor);
                        case "imagem"       -> imagem       = valor;
                    }
                }

                if (id > 0 && !nome.isEmpty()) {
                    salas.put(id, new Sala(new Item(id, nome, precoInicial, imagem)));
                    System.out.println("  ✔ Item carregado: [" + id + "] " + nome + " — R$" + precoInicial);
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Erro ao ler items.json: " + e.getMessage());
        }
    }
}