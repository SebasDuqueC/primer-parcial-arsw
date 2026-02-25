package edu.eci.arsw.threads;
import edu.eci.arsw.math.PiDigits;
import java.util.Arrays;

public class PiDigitsThread extends Thread {
    private final int start;
    private final int count;
    private byte[] result;

    public PiDigitsThread(int start, int count) {
        this.start = start;
        this.count = count;
    }
    @Override
    public void run() {
        result = PiDigits.getDigits(start, count);
    }

    public byte[] getResult() {
        return result;
    }
}