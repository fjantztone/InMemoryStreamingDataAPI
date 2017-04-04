package hashing;

/**
 * Created by heka1203 on 2017-04-01.
 */
public final class FNV {
    //32 bit output
    public static final int INIT = 0x811c9dc5;
    public static final int PRIME = 16777619;

    private int hash;
    private byte[] input;


    public void set(byte[] input){
        this.input = input;
        this.hash = 0;
    }
    public void set(String input){
        set(input.getBytes());
    }
    public int next(){
        for(byte b : input){
            hash ^= b;
            hash *= PRIME;

        }
        return hash;
    }
}
