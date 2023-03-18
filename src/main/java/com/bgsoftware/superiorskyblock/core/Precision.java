package com.bgsoftware.superiorskyblock.core;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Precision {

    private Precision() {

    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.FLOOR);
        return bd.doubleValue();
    }

}
