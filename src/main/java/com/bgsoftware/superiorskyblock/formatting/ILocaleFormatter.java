package com.bgsoftware.superiorskyblock.formatting;

import java.util.Locale;

public interface ILocaleFormatter<T> {

    String format(T value, Locale locale);

}
