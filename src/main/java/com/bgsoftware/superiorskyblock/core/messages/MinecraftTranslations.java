package com.bgsoftware.superiorskyblock.core.messages;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.io.Files;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MinecraftTranslations {

    public static final String OVERRIDE_KEY_PREFIX = "superiorskyblock.";

    private static final String[] LOADED_KEYS_PREFIXES = new String[]{
            "block.minecraft.", "item.minecraft.", "entity.minecraft.", OVERRIDE_KEY_PREFIX
    };

    private static final String VERSION_MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String RESOURCES_URL = "https://resources.download.minecraft.net/";
    // en_us is not part of the assets index, it is bundled inside the client jar.
    private static final String ENGLISH_LANGUAGE = "en_us";

    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 30000;

    private static final Gson GSON = new Gson();
    private static final AtomicBoolean downloading = new AtomicBoolean(false);
    // Languages that do not exist in minecraft, so they are not downloaded again until the server restarts.
    private static final Set<String> unavailableLanguages = ConcurrentHashMap.newKeySet();

    private static SuperiorSkyblockPlugin plugin;

    private static volatile Map<Locale, Map<String, String>> translations = Collections.emptyMap();

    private MinecraftTranslations() {

    }

    public static void reload(SuperiorSkyblockPlugin plugin) {
        MinecraftTranslations.plugin = plugin;

        // Legacy versions use a different format and different translation keys.
        if (ServerVersion.isLegacy()) {
            translations = Collections.emptyMap();
            return;
        }

        loadTranslations();

        if (plugin.getSettings().isDownloadMinecraftLanguages()) {
            List<String> missingLanguages = new LinkedList<>();
            for (Locale locale : PlayerLocales.getLocales()) {
                String languageName = getLanguageName(locale);
                if (!languageName.equals(ENGLISH_LANGUAGE) && !getLanguageFile(languageName).exists())
                    missingLanguages.add(languageName);
            }

            // Languages that were not found since the server started are not downloaded again.
            missingLanguages.removeAll(unavailableLanguages);

            if (!missingLanguages.isEmpty() && downloading.compareAndSet(false, true)) {
                BukkitExecutor.async(() -> {
                    try {
                        downloadLanguages(missingLanguages);
                    } finally {
                        downloading.set(false);
                    }
                });
            }
        }
    }

    public static boolean hasTranslations(Locale locale) {
        return translations.containsKey(locale);
    }

    public static Optional<String> translate(String translationKey, Locale locale) {
        Map<String, String> localeTranslations = translations.get(locale);
        return localeTranslations == null ? Optional.empty() : Optional.ofNullable(localeTranslations.get(translationKey));
    }

    private static synchronized void loadTranslations() {
        Map<Locale, Map<String, String>> loadedTranslations = new HashMap<>();

        for (File languageFile : Files.listFolderFiles(getLanguagesFolder(), false,
                file -> file.getName().endsWith(".json"))) {
            String languageName = Files.getFileName(languageFile);

            Locale locale;
            try {
                locale = PlayerLocales.getLocale(languageName);
            } catch (IllegalArgumentException error) {
                Log.warn("The minecraft language ", languageName, " is invalid, skipping...");
                continue;
            }

            try (Reader reader = new InputStreamReader(java.nio.file.Files.newInputStream(languageFile.toPath()),
                    StandardCharsets.UTF_8)) {
                JsonObject jsonObject = GSON.fromJson(reader, JsonObject.class);
                Map<String, String> localeTranslations = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    if (isLoadedKey(entry.getKey()) && entry.getValue().isJsonPrimitive())
                        localeTranslations.put(entry.getKey(), entry.getValue().getAsString());
                }
                loadedTranslations.put(locale, localeTranslations);
            } catch (Exception error) {
                Log.error(error, "An unexpected error occurred while loading minecraft language ", languageName, ":");
            }
        }

        translations = loadedTranslations.isEmpty() ? Collections.emptyMap() : loadedTranslations;
    }

    private static void downloadLanguages(List<String> languages) {
        try {
            Log.info("Downloading minecraft languages: ", languages);

            String version = Bukkit.getBukkitVersion().split("-")[0];
            JsonObject assetsIndex = readJson(getAssetsIndexURL(version)).getAsJsonObject("objects");

            File languagesFolder = getLanguagesFolder();
            if (!languagesFolder.exists() && !languagesFolder.mkdirs())
                throw new IOException("Failed to create folder " + languagesFolder);

            boolean downloadedAny = false;

            for (String language : languages) {
                JsonObject asset = assetsIndex.getAsJsonObject("minecraft/lang/" + language + ".json");
                if (asset == null) {
                    Log.warn("Minecraft language ", language, " does not exist, skipping...");
                    unavailableLanguages.add(language);
                    continue;
                }

                String hash = asset.get("hash").getAsString();
                HttpsURLConnection connection = openConnection(RESOURCES_URL + hash.substring(0, 2) + "/" + hash);
                try (InputStream inputStream = connection.getInputStream()) {
                    java.nio.file.Files.copy(inputStream, getLanguageFile(language).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    downloadedAny = true;
                } finally {
                    connection.disconnect();
                }
            }


            if (downloadedAny) {
                loadTranslations();
                Log.info("Successfully downloaded minecraft languages.");
            }
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while downloading minecraft languages:");
        }
    }

    private static String getAssetsIndexURL(String version) throws IOException {
        JsonObject versionManifest = readJson(VERSION_MANIFEST_URL);

        String versionURL = null;
        String latestReleaseURL = null;
        String latestRelease = versionManifest.getAsJsonObject("latest").get("release").getAsString();

        for (JsonElement versionElement : versionManifest.getAsJsonArray("versions")) {
            JsonObject versionObject = versionElement.getAsJsonObject();
            String versionId = versionObject.get("id").getAsString();
            if (versionId.equals(version)) {
                versionURL = versionObject.get("url").getAsString();
                break;
            } else if (versionId.equals(latestRelease)) {
                latestReleaseURL = versionObject.get("url").getAsString();
            }
        }

        if (versionURL == null) {
            if (latestReleaseURL == null)
                throw new IOException("Cannot find version " + version + " in the versions manifest");

            Log.warn("Cannot find version ", version, " in the versions manifest, using ", latestRelease, " instead.");
            versionURL = latestReleaseURL;
        }

        return readJson(versionURL).getAsJsonObject("assetIndex").get("url").getAsString();
    }

    private static JsonObject readJson(String url) throws IOException {
        HttpsURLConnection connection = openConnection(url);
        try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, JsonObject.class);
        } finally {
            connection.disconnect();
        }
    }

    private static HttpsURLConnection openConnection(String url) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        return connection;
    }

    private static boolean isLoadedKey(String key) {
        for (String prefix : LOADED_KEYS_PREFIXES) {
            if (key.startsWith(prefix))
                return true;
        }
        return false;
    }

    private static String getLanguageName(Locale locale) {
        String language = locale.getLanguage();
        // Older java versions convert "he" into "iw", while minecraft uses "he".
        if (language.equals("iw"))
            language = "he";
        return (language + "_" + locale.getCountry()).toLowerCase(Locale.ENGLISH);
    }

    private static File getLanguagesFolder() {
        return new File(plugin.getDataFolder(), "lang/minecraft");
    }

    private static File getLanguageFile(String languageName) {
        return new File(getLanguagesFolder(), languageName + ".json");
    }

}
