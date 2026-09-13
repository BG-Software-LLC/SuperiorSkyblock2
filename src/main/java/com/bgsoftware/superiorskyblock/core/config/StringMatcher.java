package com.bgsoftware.superiorskyblock.core.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class StringMatcher {

    private static final Pattern REGEX_META_CHARACTERS = Pattern.compile(".*[.*+?^${}()|\\[\\]\\\\].*");

    private final Set<String> exactMatches = new HashSet<>();
    private final List<Pattern> patternMatches = new ArrayList<>();

    public StringMatcher(Collection<String> values) {
        for (String value : values) {
            Pattern pattern = tryCompilePattern(value);

            if (pattern == null) {
                this.exactMatches.add(value);
            } else {
                this.patternMatches.add(pattern);
            }
        }
    }

    public boolean matches(String value) {
        if (this.exactMatches.contains(value)) {
            return true;
        }

        for (Pattern pattern : this.patternMatches) {
            if (pattern.matcher(value).matches()) {
                return true;
            }
        }

        return false;
    }

    private static Pattern tryCompilePattern(String value) {
        if (!REGEX_META_CHARACTERS.matcher(value).matches()) {
            return null;
        }

        try {
            return Pattern.compile(value);
        } catch (PatternSyntaxException ignored) {
            return null;
        }
    }

}
