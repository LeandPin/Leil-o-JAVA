package model;

import server.ClientManager;
import java.util.Timer;
import java.util.TimerTask;

public class Sala {
    private Item item;
    private Lance maiorLance;
    private int tempoRestante = 60;
    private boolean leilaoFinalizado = false;
    private Timer timer;

    public Sala(Item item) {
        this.item = item;
        // Lance inicial parte do preço definido no JSON
        this.maiorLance = new Lance(item.getPrecoInicial(), "Ninguém");
        iniciarTimer();
    }

    public synchronized boolean registrarLance(Lance novoLance) {
        if (!leilaoFinalizado && novoLance.getValor() > maiorLance.getValor()) {
            this.maiorLance = novoLance;
            resetarTimer();
            return true;
        }
        return false;
    }

    private void iniciarTimer() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                tempoRestante--;
                System.out.println("[Thread-" + Thread.currentThread().getId()
                        + "] Sala " + item.getId() + " (" + item.getNome() + ") — " + tempoRestante + "s");
                ClientManager.broadcastAll("TEMPO " + item.getId() + " " + tempoRestante);
                if (tempoRestante <= 0) finalizarLeilao();
            }
        }, 1000, 1000);
    }

    private void resetarTimer() {
        timer.cancel();
        tempoRestante = 60;
        iniciarTimer();
    }

    private void finalizarLeilao() {
        if (leilaoFinalizado) return;
        leilaoFinalizado = true;
        timer.cancel();
        String vencedor = maiorLance.getComprador().equals("Ninguém") ? "Ninguém" : maiorLance.getComprador();
        ClientManager.broadcastAll("VENDIDO " + item.getId() + " " + vencedor);
    }

    public Item  getItem()         { return item; }
    public Lance getMaiorLance()   { return maiorLance; }
    public int   getTempoRestante(){ return tempoRestante; }
    public boolean isFinalizado()  { return leilaoFinalizado; }
}