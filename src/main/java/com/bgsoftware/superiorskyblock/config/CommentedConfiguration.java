package com.bgsoftware.superiorskyblock.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public final class CommentedConfiguration extends YamlConfiguration {

    private Map<String, String> configComments = new HashMap<>();

    public void syncWithConfig(File file, InputStream resource, String... ignoredSections){
        CommentedConfiguration cfg = loadConfiguration(resource);
        if(syncConfigurationSection(cfg, cfg.getConfigurationSection(""), Arrays.asList(ignoredSections)) && file != null)
            save(file);
    }

    public void setComment(String key, String comment){
        if(comment == null)
            configComments.remove(key);
        else
            configComments.put(key, comment);
    }

    public String getComment(String key){
        return getComment(key, null);
    }

    public String getComment(String key, String def){
        return configComments.getOrDefault(key, def);
    }

    public boolean containsComment(String key){
        return getComment(key) != null;
    }

    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {
        super.loadFromString(contents);

        String[] lines = contents.split("\n");
        int currentIndex = 0;

        StringBuilder comments = new StringBuilder();
        String currentSection = "";

        while(currentIndex < lines.length){
            while(currentIndex < lines.length && isComment(lines[currentIndex])){
                comments.append(lines[currentIndex]).append("\n");
                currentIndex++;
            }

            if(currentIndex >= lines.length)
                break;

            //Not comment - maybe a section?
            if(isNewSection(lines[currentIndex])){
                currentSection = getSectionPath(this, lines[currentIndex], currentSection);

                if(comments.length() > 0)
                    setComment(currentSection, comments.toString().substring(0, comments.length() - 1));

                comments = new StringBuilder();
            }

            currentIndex++;
        }
    }

    @Override
    public String saveToString() {
        this.options().header(null);
        List<String> lines = new ArrayList<>(Arrays.asList(super.saveToString().split("\n")));

        int currentIndex = 0;

        String currentSection = "";

        while(currentIndex < lines.size()){
            String line = lines.get(currentIndex);

            if(isNewSection(line)){
                currentSection = getSectionPath(this, line, currentSection);
                if(containsComment(currentSection)) {
                    lines.add(currentIndex, getComment(currentSection));
                    currentIndex++;
                }
            }

            currentIndex++;
        }

        StringBuilder contents = new StringBuilder();
        for(String line : lines)
            contents.append("\n").append(line);

        return contents.length() == 0 ? "" : contents.substring(1);
    }

    @Override
    public void save(File file){
        try {
            super.save(file);
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private boolean syncConfigurationSection(CommentedConfiguration commentedConfig, ConfigurationSection section, List<String> ignoredSections){
        boolean changed = false, changedValue = false;

        for (String key : section.getKeys(false)) {
            String path = section.getCurrentPath().isEmpty() ? key : section.getCurrentPath() + "." + key;

            if (section.isConfigurationSection(key)) {
                boolean isIgnored = ignoredSections.stream().anyMatch(path::contains);
                boolean containsSection = contains(path);
                if(!containsSection || !isIgnored) {
                    changed = syncConfigurationSection(commentedConfig, section.getConfigurationSection(key), ignoredSections) || changed;
                    if(!containsSection)
                        changedValue = true;
                }
            }

            if (!contains(path)) {
                set(path, section.get(key));
                changed = true;
                changedValue = true;
            }

            if (commentedConfig.containsComment(path) && !commentedConfig.getComment(path).equals(getComment(path))) {
                setComment(path, commentedConfig.getComment(path));
                changed = true;
            }

        }

        if(changedValue){
            correctIndexes(section, getConfigurationSection(section.getCurrentPath()));
        }

        return changed;
    }

    public static CommentedConfiguration loadConfiguration(File file) {
        try {
            FileInputStream stream = new FileInputStream(file);
            return loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }catch(FileNotFoundException ex){
            Bukkit.getLogger().warning("File " + file.getName() + " doesn't exist.");
            return new CommentedConfiguration();
        }
    }

    public static CommentedConfiguration loadConfiguration(InputStream stream) {
        return loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    public static CommentedConfiguration loadConfiguration(Reader reader) {
        CommentedConfiguration config = new CommentedConfiguration();

        try(BufferedReader bufferedReader = reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader)){
            StringBuilder contents = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null) {
                contents.append(line).append('\n');
            }

            config.loadFromString(contents.toString());
        } catch (IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
        }

        return config;
    }

    private static boolean isNewSection(String line){
        String trimLine = line.trim();
        return trimLine.contains(": ") || trimLine.endsWith(":");
    }

    private static String getSectionPath(CommentedConfiguration cfg, String line, String currentSection){
        String newSection = line.trim().split(": ")[0];

        if(newSection.endsWith(":"))
            newSection = newSection.substring(0, newSection.length() - 1);

        if(newSection.startsWith("."))
            newSection = newSection.substring(1);

        if(newSection.startsWith("'") && newSection.endsWith("'"))
            newSection = newSection.substring(1, newSection.length() - 1);

        if(!currentSection.isEmpty() && cfg.contains(currentSection + "." + newSection)){
            newSection = currentSection + "." + newSection;
        }

        else{
            String parentSection = currentSection;

            //Get the parent section that has the new section
            //noinspection StatementWithEmptyBody
            while(!cfg.contains((parentSection = getParentPath(parentSection)) + "." + newSection) && !parentSection.isEmpty());

            newSection = parentSection.trim().isEmpty() ? newSection : parentSection + "." + newSection;
        }

        return newSection;
    }

    private static boolean isComment(String line){
        String trimLine = line.trim();
        return trimLine.startsWith("#") || trimLine.isEmpty();
    }

    private static String getParentPath(String path){
        return path.contains(".") ? path.substring(0, path.lastIndexOf('.')) : "";
    }

    private static void correctIndexes(ConfigurationSection section, ConfigurationSection target){
        List<Pair<String, Object>> sectionMap = getSectionMap(section), targetMap = getSectionMap(target), correctOrder = new ArrayList<>();

        for (Pair<String, Object> entry : sectionMap) {
            correctOrder.add(entry);
            targetMap.remove(entry);
        }

        correctOrder.addAll(targetMap);

        clearConfiguration(target);

        for(Pair<String, Object> entry : correctOrder)
            target.set(entry.key, entry.value);
    }

    private static List<Pair<String, Object>> getSectionMap(ConfigurationSection section){
        List<Pair<String, Object>> list = new ArrayList<>();

        for(String key : section.getKeys(false)) {
            list.add(new Pair<>(key, section.get(key)));
        }

        return list;
    }

    private static void clearConfiguration(ConfigurationSection section){
        for(String key : section.getKeys(false))
            section.set(key, null);
    }

    private static class Pair<K, V>{

        private final K key;
        private final V value;

        Pair(K key, V value){
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Pair && key.equals(((Pair) obj).key) && value.equals(((Pair) obj).value);
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }

}
