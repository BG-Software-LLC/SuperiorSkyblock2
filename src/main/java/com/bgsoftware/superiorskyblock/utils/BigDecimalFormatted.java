package com.bgsoftware.superiorskyblock.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public final class BigDecimalFormatted extends BigDecimal {

    public static BigDecimalFormatted ZERO = new BigDecimalFormatted(0);

    private BigDecimalFormatted(int i){
        super(i);
    }

    private BigDecimalFormatted(double d){
        super(d);
    }

    private BigDecimalFormatted(String str){
        super(str);
    }

    @Override
    public BigDecimalFormatted add(BigDecimal augend) {
        return BigDecimalFormatted.of(super.add(augend));
    }

    @Override
    public BigDecimalFormatted subtract(BigDecimal subtrahend) {
        return BigDecimalFormatted.of(super.subtract(subtrahend));
    }


    @Override
    public BigDecimalFormatted setScale(int newScale, RoundingMode roundingMode) {
        BigDecimal bigDecimal = super.setScale(newScale, roundingMode);
        return bigDecimal instanceof BigDecimalFormatted ? (BigDecimalFormatted) bigDecimal : BigDecimalFormatted.of(bigDecimal);
    }

    @Override
    public double doubleValue() {
        try{
            return super.doubleValue();
        }catch(NumberFormatException ex){
            return Double.parseDouble(getAsString());
        }
    }

    @Override
    public String toString() {
        try {
            return StringUtils.format(this);
        }catch(Exception ex){
            return "";
        }
    }
    
    public String getAsString(){
        return super.toString();
    }

    public static BigDecimalFormatted of(int i){
        return new BigDecimalFormatted(i);
    }

    public static BigDecimalFormatted of(double d){
        return new BigDecimalFormatted(d);
    }

    public static BigDecimalFormatted of(String str){
        return new BigDecimalFormatted(str == null || str.isEmpty() ? "0" : str);
    }

    public static BigDecimalFormatted of(BigDecimal bigDecimal){
        if(bigDecimal instanceof BigDecimalFormatted)
            return (BigDecimalFormatted) bigDecimal;
        if(bigDecimal.toString().contains("-")) bigDecimal = bigDecimal.negate().negate();
        return new BigDecimalFormatted(bigDecimal.toString());
    }

    public static BigDecimalFormatted of(BigInteger bigInteger){
        return new BigDecimalFormatted(bigInteger.toString());
    }

}
