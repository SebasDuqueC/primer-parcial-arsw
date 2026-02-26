package edu.eci.arsw.math;

import edu.eci.arsw.threads.PiDigitsThread;
import java.util.ArrayList;
import java.util.Scanner;

///  <summary>
///  An implementation of the Bailey-Borwein-Plouffe formula for calculating hexadecimal
///  digits of pi.
///  https://en.wikipedia.org/wiki/Bailey%E2%80%93Borwein%E2%80%93Plouffe_formula
///  *** Translated from C# code: https://github.com/mmoroney/DigitsOfPi ***
///  </summary>
public class PiDigits {

    private static int DigitsPerSum = 8;
    private static double Epsilon = 1e-17;

    private static final Object lock = new Object();
    private static boolean pausa = false;

    public static void pausar() {
        pausa = true;
    }

    public static void reanudar() {
        pausa = false;
        synchronized (lock) {

            lock.notifyAll();
        }

    }

    
    /**
     * Returns a range of hexadecimal digits of pi (sequential).
     * @param start The starting location of the range.
     * @param count The number of digits to return
     * @return An array containing the hexadecimal digits.
     */
    public static byte[] getDigits(int start, int count) {
        if (start < 0) {
            throw new RuntimeException("Invalid Interval");
        }

        if (count < 0) {
            throw new RuntimeException("Invalid Interval");
        }

        byte[] digits = new byte[count];
        double sum = 0;

        for (int i = 0; i < count; i++) {
            
            synchronized (lock) {
                if (pausa) {
                    System.out.println(i + "digitos");
                    while (pausa) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            //
                        }
                    }
                }
            }


            if (i % DigitsPerSum == 0) {
                sum = 4 * sum(1, start)
                        - 2 * sum(4, start)
                        - sum(5, start)
                        - sum(6, start);

                start += DigitsPerSum;
            }

            sum = 16 * (sum - Math.floor(sum));
            digits[i] = (byte) sum;
        }

        return digits;
    }


    public static byte[] getDigits(int start, int count, int N) {
        
        ArrayList<PiDigitsThread> threads = new ArrayList<>();
        int remainder = count % N;
        boolean fin = false;
        int digitsPerThread = count / N;
        int currentStart = start;
        
        for (int i = 0; i < N; i++) {
            int threadCount = digitsPerThread;
            //System.out.print(threadCount);
            if (i < remainder) {
                threadCount = threadCount + 1;
            }
            PiDigitsThread thread = new PiDigitsThread(currentStart, threadCount);
            thread.start();
            threads.add(thread);
            currentStart += threadCount;
            

        }

        while (!fin) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                //
            }
            fin = true;
            for (PiDigitsThread thread : threads) {
                if (thread.isAlive()) {
                    fin = false;
                    break;
                }
            }
            if (!fin) {
                PiDigits.pausar();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {}
                System.out.println("ENTER");
                Scanner scanner = new Scanner(System.in);
                scanner.nextLine();
                PiDigits.reanudar();
            }
        }

        byte[] digits = new byte[count];
        int offset = 0;
        for (int i = 0; i < N; i++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                //
            }
            PiDigitsThread res = threads.get(i);
            byte[] partial = res.getResult();
            System.arraycopy(partial, 0, digits, offset, partial.length);
            offset += partial.length;
        }

        return digits;
    }

    /// <summary>
    /// Returns the sum of 16^(n - k)/(8 * k + m) from 0 to k.
    /// </summary>
    /// <param name="m"></param>
    /// <param name="n"></param>
    /// <returns></returns>
    private static double sum(int m, int n) {
        double sum = 0;
        int d = m;
        int power = n;

        while (true) {
            double term;

            if (power > 0) {
                term = (double) hexExponentModulo(power, d) / d;
            } else {
                term = Math.pow(16, power) / d;
                if (term < Epsilon) {
                    break;
                }
            }

            sum += term;
            power--;
            d += 8;
        }

        return sum;
    }

    /// <summary>
    /// Return 16^p mod m.
    /// </summary>
    /// <param name="p"></param>
    /// <param name="m"></param>
    /// <returns></returns>
    private static int hexExponentModulo(int p, int m) {
        int power = 1;
        while (power * 2 <= p) {
            power *= 2;
        }

        int result = 1;

        while (power > 0) {
            if (p >= power) {
                result *= 16;
                result %= m;
                p -= power;
            }

            power /= 2;

            if (power > 0) {
                result *= result;
                result %= m;
            }
        }

        return result;
    }

}
