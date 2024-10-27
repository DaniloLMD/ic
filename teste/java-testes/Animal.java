public class Animal{
    public String nome;

    Animal(String nome){
        this.nome = nome;
    }

    public String getNome(){
        return nome;
    }

    public String som(){
        return "Som generico";
    }
}