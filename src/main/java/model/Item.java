package model;

public class Item {
    private int id;
    private String nome;
    private double precoInicial;
    private String imagemPath; // caminho relativo: "images/watch.jpg"

    public Item(int id, String nome, double precoInicial, String imagemPath) {
        this.id          = id;
        this.nome        = nome;
        this.precoInicial = precoInicial;
        this.imagemPath  = imagemPath;
    }

    public int    getId()          { return id; }
    public String getNome()        { return nome; }
    public double getPrecoInicial(){ return precoInicial; }
    public String getImagemPath()  { return imagemPath; }
}