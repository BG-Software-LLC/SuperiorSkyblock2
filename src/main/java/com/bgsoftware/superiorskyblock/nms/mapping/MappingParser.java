package com.bgsoftware.superiorskyblock.nms.mapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MappingParser {

    private static final Pattern NEW_CLASS_REMAP_PATTERN = Pattern.compile("^([^ ]+) -> [^ ]+:$");
    private static final Pattern FIELD_REMAP_PATTERN = Pattern.compile("^[ ]{4}[^ ]+ ([^ (]+)+ -> ([^ ]+)$");
    private static final Pattern METHOD_REMAP_PATTERN = Pattern.compile("^[ ]{4}[^ ]+ ([^ ]+)\\(.*\\) -> ([^ ]+)$");

    private MappingParser() {

    }

    public static Map<String, Remapped> parseRemappedMap(File mappingsFile) throws IOException {
        Map<String, Remapped> remappedMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(mappingsFile))) {
            Remapped currentRemapped = null;
            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.startsWith("#")) // Comment
                    continue;

                Matcher matcher;

                if ((matcher = NEW_CLASS_REMAP_PATTERN.matcher(currentLine)).matches()) {
                    currentRemapped = new Remapped();
                    remappedMap.put(matcher.group(1), currentRemapped);
                } else if (currentRemapped != null) {
                    if ((matcher = FIELD_REMAP_PATTERN.matcher(currentLine)).matches()) {
                        String fieldName = matcher.group(1);
                        String obfuscatedName = matcher.group(2);
                        currentRemapped.put(Remap.Type.FIELD, fieldName, obfuscatedName);
                    } else if ((matcher = METHOD_REMAP_PATTERN.matcher(currentLine)).matches()) {
                        String methodName = matcher.group(1);
                        String obfuscatedName = matcher.group(2);
                        currentRemapped.put(Remap.Type.METHOD, methodName, obfuscatedName);
                    }
                }
            }
        }

        return remappedMap;
    }

}
