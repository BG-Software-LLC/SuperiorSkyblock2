package com.bgsoftware.superiorskyblock.core.engine;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class NashornEngineDownloader {

    private static final Gson GSON = new Gson();
    private static final String JENKINS_URL = "https://hub.bg-software.com/job/SuperiorSkyblock2%20-%20NashornEngine%20Module%20-%20Dev%20Builds/lastSuccessfulBuild/";
    private static final String JENKINS_API_ENDPOINT = JENKINS_URL + "api/json";
    private static final String JENKINS_ARTIFACTS_ENDPOINT = JENKINS_URL + "artifact/target/";

    public static boolean downloadEngine(SuperiorSkyblockPlugin plugin) {
        try {
            Log.info("Seems like you are missing a nashorn engine. Attempting to download one remotely...");
            deleteExistingModule(plugin);
            JsonObject apiResponse = readJenkinsJsonAPI();
            JsonObject artifact = apiResponse.getAsJsonArray("artifacts").get(0).getAsJsonObject();
            String fileName = artifact.get("fileName").getAsString();
            File engineFile = downloadEngine(plugin, fileName);
            Log.info("Successfully downloaded the nashorn engine file, enabling it...");
            PluginModule pluginModule = plugin.getModules().registerModule(engineFile);
            plugin.getModules().enableModule(pluginModule);
            return true;
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while downloading nashorn engine:");
            return false;
        }
    }

    private static void deleteExistingModule(SuperiorSkyblockPlugin plugin) {
        PluginModule nashornEngineModule = plugin.getModules().getModule("nashorn-engine");

        if (nashornEngineModule == null)
            return;

        plugin.getModules().unregisterModule(nashornEngineModule);

        try {
            nashornEngineModule.getModuleFile().delete();
        } catch (Exception ignored) {
        }
    }

    private static JsonObject readJenkinsJsonAPI() throws IOException {
        HttpsURLConnection conn = (HttpsURLConnection) new URL(JENKINS_API_ENDPOINT).openConnection();
        conn.setRequestMethod("GET");

        StringBuilder jsonContents = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null)
                jsonContents.append(line);
        }

        return GSON.fromJson(jsonContents.toString(), JsonObject.class);
    }

    private static File downloadEngine(SuperiorSkyblockPlugin plugin, String engineName) throws IOException {
        String downloadURL = JENKINS_ARTIFACTS_ENDPOINT + engineName;
        File engineFile = new File(plugin.getDataFolder(), "modules/" + engineName);

        try (InputStream inputStream = new URL(downloadURL).openStream()) {
            Files.copy(inputStream, Paths.get(engineFile.toURI()));
        }

        return engineFile;
    }

}
