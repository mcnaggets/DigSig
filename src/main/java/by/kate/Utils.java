package by.kate;

import java.nio.ByteBuffer;

public class Utils {

    private Utils() {
    }

    public static byte[] toByteArray(double[] doubleArray) {
        byte[] bytes = new byte[doubleArray.length];
        for (int i = 0; i < doubleArray.length; i++) {
            bytes[i] = (byte) doubleArray[i];
        }
        return bytes;
    }

    public static double[] toDoubleArray(byte[] byteArray) {
        double[] doubles = new double[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            doubles[i] = byteArray[i];
        }
        return doubles;
    }
}
