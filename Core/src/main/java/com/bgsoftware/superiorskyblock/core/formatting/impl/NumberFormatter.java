package com.bgsoftware.superiorskyblock.core.formatting.impl;

import com.bgsoftware.superiorskyblock.core.formatting.IFormatter;
import com.bgsoftware.superiorskyblock.core.logging.Log;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberFormatter implements IFormatter<Number> {

    private static final NumberFormatter INSTANCE = new NumberFormatter();

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[a-z]{2}[_|-][A-Z]{2}$");
    private static final char SPACE_ASCII = 160;

    private static Pattern DECIMAL_PATTERN;
    private static DecimalFormat numberFormatter;
    private static char decimalSeparator;

    public static void setNumberFormatter(String numberFormat) {
        numberFormat = numberFormat.replace("_", "-");

        if (!NUMBER_PATTERN.matcher(numberFormat).matches()) {
            Log.warn("The number format \"", numberFormat, "\" is invalid. Using default one: en-US.");
            numberFormat = "en-US";
        }

        String[] numberFormatSections = numberFormat.split("-");
        numberFormatter = (DecimalFormat) NumberFormat.getInstance(new java.util.Locale(numberFormatSections[0], numberFormatSections[1]));
        numberFormatter.setGroupingUsed(true);
        numberFormatter.setMinimumFractionDigits(2);
        numberFormatter.setMaximumFractionDigits(2);
        numberFormatter.setRoundingMode(RoundingMode.FLOOR);

        decimalSeparator = numberFormatter.getDecimalFormatSymbols().getDecimalSeparator();

        DECIMAL_PATTERN = Pattern.compile("(.*)" + Pattern.quote(decimalSeparator + "") + "(\\d)0");
    }

    public static NumberFormatter getInstance() {
        return INSTANCE;
    }

    private NumberFormatter() {

    }

    @Override
    public String format(Number value) {
        //Because of some issues with formatting, spaces are converted to ascii 160.
        String formattedNumber = numberFormatter.format(value).replace(SPACE_ASCII, ' ');

        Matcher matcher;

        if (formattedNumber.endsWith(decimalSeparator + "00")) {
            return formattedNumber.replace(decimalSeparator + "00", "");
        } else if ((matcher = DECIMAL_PATTERN.matcher(formattedNumber)).matches()) {
            return formattedNumber.replaceAll(Pattern.quote(decimalSeparator + "") + "(\\d)0", decimalSeparator + matcher.group(2));
        }

        return formattedNumber;
    }

}
