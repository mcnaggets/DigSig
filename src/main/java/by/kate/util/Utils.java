package by.kate.util;

import org.apache.commons.math3.util.BigReal;

public class Utils {

    private Utils() {
    }

    public static BigReal[] toBigRealArray(byte[] byteArray) {
        BigReal[] bigReals = new BigReal[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            bigReals[i] = new BigReal(byteArray[i]);
        }
        return bigReals;
    }

}
