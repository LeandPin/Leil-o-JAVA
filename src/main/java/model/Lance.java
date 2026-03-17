package model;

public class Lance {
    private double valor;
    private String comprador;
    private long timestamp;

    public Lance(double valor, String comprador) {
        this.valor = valor;
        this.comprador = comprador;
        this.timestamp = System.currentTimeMillis();
    }

    public double getValor() {
        return valor;
    }

    public String getComprador() {
        return comprador;
    }

    @Override
    public String toString() {
        return "R$ " + valor + " por " + comprador;
    }
}