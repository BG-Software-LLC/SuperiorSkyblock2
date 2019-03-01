package com.bgsoftware.superiorskyblock.utils;

import com.bgsoftware.superiorskyblock.utils.jnbt.DoubleTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.IntTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.StringTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.Tag;

import java.math.BigDecimal;
import java.math.BigInteger;

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
    public String toString() {
        return StringUtil.format(this);
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
        return new BigDecimalFormatted(str);
    }

    public static BigDecimalFormatted of(BigDecimal bigDecimal){
        return new BigDecimalFormatted(bigDecimal.toString());
    }

    public static BigDecimalFormatted of(BigInteger bigInteger){
        return new BigDecimalFormatted(bigInteger.toString());
    }

    public static BigDecimalFormatted of(Tag tag){
        if(tag instanceof IntTag)
            return of(((IntTag) tag).getValue());
        else if(tag instanceof DoubleTag)
            return of(((DoubleTag) tag).getValue());
        else if(tag instanceof StringTag)
            return of(((StringTag) tag).getValue());

        throw new IllegalArgumentException("Cannot convert " + tag.getClass() + " into BigDecimalFormatted");
    }

}
