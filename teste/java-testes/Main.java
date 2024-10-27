import java.util.HashMap;
import java.util.Map;

public class Main{
    public static void main(String[] args) {
        // Animal[] v = new Animal[5];

        // v[0] = new Pato("PATOL");
        // v[1] = new Pombo("POMBOL");
        // v[2] = new Animal("G.");
        // v[3] = new Pato("PATO & LINO JR.");
        // v[4] = new Pombo("PPPOOMBO");

        // for(int i = 0; i < v.length; i++){
        //     System.out.println(v[i].getNome() + " faz " + v[i].som());
        //     if(v[i] instanceof Pombo){
        //         Pombo p = (Pombo) v[i];
        //         p.cagar();
        //     }
        // }

        Map<Integer, Integer> mp = new HashMap<>();
        int[] v = new int[5];
        
        for(int i = 0; i < 5; i ++){
            v[i] = i * 10;
            mp.put(i, i * 10);
        }

        System.out.println("mp = " + mp);
        System.out.println("v = " + v);
    }
};