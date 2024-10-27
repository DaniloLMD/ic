public class Pombo extends Animal{
    Pombo(String nome){
        super(nome);
    }

    @Override
    public String som(){
        return "PRU";
    }

    public void cagar(){
        System.out.println(nome + " cagou");
    }
}
