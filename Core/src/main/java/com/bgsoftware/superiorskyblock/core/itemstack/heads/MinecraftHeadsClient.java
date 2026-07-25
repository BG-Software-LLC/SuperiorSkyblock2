package com.bgsoftware.superiorskyblock.core.itemstack.heads;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.DynamicArray;
import com.bgsoftware.superiorskyblock.core.logging.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinecraftHeadsClient {

    private static final Pattern MINECRAFT_HEADS_PATTERN = Pattern.compile("minecraft-heads:(\\d+)");
    private static final int EXPECTED_HEADS_SIZE = 50_000;
    private static final String MINECRAFT_HEADS_DATABASE_URL = "https://minecraft-heads.com/csv/2020-01-31-IUgRbJoHRbVhjKnOlkmH/Custom-Head-DB.csv";

    public static Optional<Integer> getMinecraftHeadsTextureId(String texture) {
        Matcher matcher = MINECRAFT_HEADS_PATTERN.matcher(texture);
        return matcher.matches() ? Optional.of(Integer.parseInt(matcher.group(1))) : Optional.empty();
    }

    private final List<String> cachedHeads;

    public MinecraftHeadsClient() {
        this.cachedHeads = fetchHeadsDatabase();
    }

    @Nullable
    public String getTexture(int id) {
        int index = id - 1;
        return index < cachedHeads.size() ? cachedHeads.get(index) : null;
    }

    private static List<String> fetchHeadsDatabase() {
        DynamicArray<String> heads = new DynamicArray<>(EXPECTED_HEADS_SIZE);
        boolean addedAnyHead = false;

        try {
            URLConnection connection = new URL(MINECRAFT_HEADS_DATABASE_URL).openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String texture;
                while ((texture = reader.readLine()) != null) {
                    String[] textureSegments = texture.split(";");
                    if (textureSegments.length >= 4) {
                        int id;
                        try {
                            id = Integer.parseInt(textureSegments[1]);
                        } catch (NumberFormatException ignored) {
                            continue;
                        }
                        String textureValue = textureSegments[3];
                        String textureUrl = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + textureValue + "\"}}}";
                        heads.set(id - 1, Base64.getEncoder().encodeToString(textureUrl.getBytes(StandardCharsets.UTF_8)));
                        addedAnyHead = true;
                    }
                }
            }
        } catch (Exception error) {
            Log.error(error, "Failed to obtain heads from minecraft-heads:");
        }

        return addedAnyHead ? heads.toList() : Collections.emptyList();
    }

}
