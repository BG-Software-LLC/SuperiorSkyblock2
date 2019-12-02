/*******************************************************************************
 *
 *     CommentedConfiguration
 *     Developed by Ome_R
 *
 *     You may use the resource and/or modify it - but not
 *     claiming it as your own work. You are not allowed
 *     to remove this message, unless being permitted by
 *     the developer of the resource.
 *
 *     Spigot: https://www.spigotmc.org/resources/authors/ome_r.25629/
 *     MC-Market: https://www.mc-market.org/resources/authors/40228/
 *     Github: https://github.com/OmerBenGera?tab=repositories
 *     Website: https://bg-software.com/
 *
 *******************************************************************************/

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

public final class CommentedConfiguration extends YamlConfiguration {

    /**
     * Holds all comments for the config.
     * Can be accessed by using the methods that are provided
     * by the class.
     */
    private final Map<String, String> configComments = new HashMap<>();

    /**
     * Sync the config with another resource.
     * This method can be used as an auto updater for your config files.
     * @param file The file to save changes into if there are any.
     * @param resource The resource to sync with. Can be provided by JavaPlugin#getResource
     * @param ignoredSections An array of sections that will be ignored, and won't get updated
     *                        if they are already exist in the config. If they are not in the
     *                        config, they will be synced with the resource's config.
     */
    public void syncWithConfig(File file, InputStream resource, String... ignoredSections) throws IOException{
        CommentedConfiguration cfg = loadConfiguration(resource);
        if(syncConfigurationSection(cfg, cfg.getConfigurationSection(""), Arrays.asList(ignoredSections)) && file != null)
            save(file);
    }

    /**
     * Set a new comment to a path.
     * You can remove comments by providing a null as a comment argument.
     * @param path The path to set the comment to.
     * @param comment The comment to set. Supports multiple lines (use \n as a spacer).
     */
    public void setComment(String path, String comment){
        if(comment == null)
            configComments.remove(path);
        else
            configComments.put(path, comment);
    }

    /**
     * Get a comment of a path.
     * @param path The path to get the comment of.
     * @return Returns a string that contains the comment. If no comment exists, null will be returned.
     */
    public String getComment(String path){
        return getComment(path, null);
    }

    /**
     * Get a comment of a path with a default value if no comment exists.
     * @param path The path to get the comment of.
     * @param def A default comment that will be returned if no comment exists for the path.
     * @return Returns a string that contains the comment. If no comment exists, the def value will be returned.
     */
    public String getComment(String path, String def){
        return configComments.getOrDefault(path, def);
    }

    /**
     * Checks whether or not a path has a comment.
     * @param path The path to check.
     * @return Returns true if there's an existing comment, otherwise false.
     */
    public boolean containsComment(String path){
        return getComment(path) != null;
    }

    /**
     * Load all data related to the config file - keys, values and comments.
     * @param contents The contents of the file.
     * @throws InvalidConfigurationException if the contents are invalid.
     */
    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {
        //Load data of the base yaml (keys and values).
        super.loadFromString(contents);

        //Parse the contents into lines.
        String[] lines = contents.split("\n");
        int currentIndex = 0;

        //Variables that are used to track progress.
        StringBuilder comments = new StringBuilder();
        String currentSection = "";

        while(currentIndex < lines.length){
            //Checking if the current line is a comment.
            if(isComment(lines[currentIndex])){
                //Adding the comment to the builder with an enter at the end.
                comments.append(lines[currentIndex]).append("\n");
            }

            //Checking if the current line is a valid new section.
            else if(isNewSection(lines[currentIndex])){
                //Parsing the line into a full-path.
                currentSection = getSectionPath(this, lines[currentIndex], currentSection);

                //If there is a valid comment for the section.
                if(comments.length() > 0)
                    //Adding the comment.
                    setComment(currentSection, comments.toString().substring(0, comments.length() - 1));

                //Reseting the comment variable for further usage.
                comments = new StringBuilder();
            }

            //Skipping to the next line.
            currentIndex++;
        }
    }

    /**
     * Parsing all the data (keys, values and comments) into a valid string, that will be written into a file later.
     * @return A string that contains all the data, ready to be written into a file.
     */
    @Override
    public String saveToString() {
        //First, we set headers to null - as we will handle all comments, including headers, in this method.
        this.options().header(null);
        //Get the string of the data (keys and values) and parse it into an array of lines.
        List<String> lines = new ArrayList<>(Arrays.asList(super.saveToString().split("\n")));

        //Variables that are used to track progress.
        int currentIndex = 0;
        String currentSection = "";

        while(currentIndex < lines.size()){
            String line = lines.get(currentIndex);

            //Checking if the line is a new section.
            if(isNewSection(line)){
                //Parsing the line into a full-path.
                currentSection = getSectionPath(this, line, currentSection);
                //Checking if that path contains a comment
                if(containsComment(currentSection)) {
                    //Adding the comment to the lines array at that index (adding it one line before the actual line)
                    lines.add(currentIndex, getComment(currentSection));
                    //Skipping one line so the pointer will point to the line we checked again.
                    currentIndex++;
                }
            }

            //Skipping to the next line.
            currentIndex++;
        }

        //Parsing the array of lines into a one single string.
        StringBuilder contents = new StringBuilder();
        for(String line : lines)
            contents.append("\n").append(line);

        return contents.length() == 0 ? "" : contents.substring(1);
    }

    /**
     * Sync a specific configuration section with another one, recursively.
     * @param commentedConfig The config that contains the data we need to sync with.
     * @param section The current section that we sync.
     * @param ignoredSections A list of ignored sections that won't be synced (unless not found in the file).
     * @return Returns true if there were any changes, otherwise false.
     */
    private boolean syncConfigurationSection(CommentedConfiguration commentedConfig, ConfigurationSection section, List<String> ignoredSections){
        //Variables that are used to track progress.
        boolean changed = false;

        //Going through all the keys of the section.
        for (String key : section.getKeys(false)) {
            //Parsing the key into a full-path.
            String path = section.getCurrentPath().isEmpty() ? key : section.getCurrentPath() + "." + key;

            //Checking if the key is also a section.
            if (section.isConfigurationSection(key)) {
                //Checking if the section is ignored.
                boolean isIgnored = ignoredSections.stream().anyMatch(path::contains);
                //Checking if the config contains the section.
                boolean containsSection = contains(path);
                //If the config doesn't contain the section, or it's not ignored - we will sync data.
                if(!containsSection || !isIgnored) {
                    //Syncing data and updating the changed variable.
                    changed = syncConfigurationSection(commentedConfig, section.getConfigurationSection(key), ignoredSections) || changed;
                }
            }

            //Checking if the config contains the path (not a section).
            else if (!contains(path)) {
                //Saving the value of the key into the config.
                set(path, section.get(key));
                //Updating variables.
                changed = true;
            }

            //Checking if there is a valid comment for the path, and also making sure the comments are not the same.
            if (commentedConfig.containsComment(path) && !commentedConfig.getComment(path).equals(getComment(path))) {
                //Saving the comment of the key into the config.
                setComment(path, commentedConfig.getComment(path));
                //Updating variable.
                changed = true;
            }

        }

        /*Keys cannot be ordered easily, so we need to do some tricks to make sure
        all of the keys are ordered correctly (and the new config will look the same
        as the resource that was provided).*/

        //Checking if there was a value that had been added into the config
        if(changed)
            correctIndexes(section, getConfigurationSection(section.getCurrentPath()));

        return changed;
    }

    /**
     * Load a config from a file.
     * @param file The file to load the config from.
     * @return A new instance of CommentedConfiguration contains all the data (keys, values and comments).
     */
    public static CommentedConfiguration loadConfiguration(File file) {
        try {
            FileInputStream stream = new FileInputStream(file);
            return loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }catch(FileNotFoundException ex){
            Bukkit.getLogger().warning("File " + file.getName() + " doesn't exist.");
            return new CommentedConfiguration();
        }
    }

    /**
     * Load a config from an input-stream, which is used for resources that are obtained using JavaPlugin#getResource.
     * @param inputStream The input-stream to load the config from.
     * @return A new instance of CommentedConfiguration contains all the data (keys, values and comments).
     */
    public static CommentedConfiguration loadConfiguration(InputStream inputStream) {
        return loadConfiguration(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    /**
     * Load a config from a reader (used for files and streams together).
     * @param reader The reader to load the config from.
     * @return A new instance of CommentedConfiguration contains all the data (keys, values and comments).
     */
    public static CommentedConfiguration loadConfiguration(Reader reader) {
        //Creating a blank instance of the config.
        CommentedConfiguration config = new CommentedConfiguration();

        //Parsing the reader into a BufferedReader for an easy reading of it.
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

    /**
     * Checks if a line is a new section or not.
     * Sadly, that's not possible to use ":" as a spacer between key and value, and ": " must be used.
     * @param line The line to check.
     * @return True if the line is a new section, otherwise false.
     */
    private static boolean isNewSection(String line){
        String trimLine = line.trim();
        return trimLine.contains(": ") || trimLine.endsWith(":");
    }

    /**
     * Creates a full path of a line.
     * @param commentedConfig The config to get the path from.
     * @param line The line to get the path of.
     * @param currentSection The last known section.
     * @return The full path of the line.
     */
    private static String getSectionPath(CommentedConfiguration commentedConfig, String line, String currentSection){
        //Removing all spaces and getting the name of the section.
        String newSection = line.trim().split(": ")[0];

        //Parsing some formats to make sure having a plain name.
        if(newSection.endsWith(":"))
            newSection = newSection.substring(0, newSection.length() - 1);
        if(newSection.startsWith("."))
            newSection = newSection.substring(1);
        if(newSection.startsWith("'") && newSection.endsWith("'"))
            newSection = newSection.substring(1, newSection.length() - 1);

        //Checking if the new section is a child-section of the currentSection.
        if(!currentSection.isEmpty() && commentedConfig.contains(currentSection + "." + newSection)){
            newSection = currentSection + "." + newSection;
        }

        //Looking for the parent of the the new section.
        else{
            String parentSection = currentSection;

            /*Getting the parent of the new section. The loop will stop in one of the following situations:
            1) The parent is empty - which means we have no where to go, as that's the root section.
            2) The config contains a valid path that was built with <parent-section>.<new-section>.*/
            while(!parentSection.isEmpty() &&
                    !commentedConfig.contains((parentSection = getParentPath(parentSection)) + "." + newSection));

            //Parsing and building the new full path.
            newSection = parentSection.trim().isEmpty() ? newSection : parentSection + "." + newSection;
        }

        return newSection;
    }

    /**
     * Checks if a line represents a comment or not.
     * @param line The line to check.
     * @return True if the line is a comment (stars with # or it's empty), otherwise false.
     */
    private static boolean isComment(String line){
        String trimLine = line.trim();
        return trimLine.startsWith("#") || trimLine.isEmpty();
    }

    /**
     * Get the parent path of the provided path, by removing the last '.' from the path.
     * @param path The path to check.
     * @return The parent path of the provided path.
     */
    private static String getParentPath(String path){
        return path.contains(".") ? path.substring(0, path.lastIndexOf('.')) : "";
    }

    /**
     * Convert key-indexes of a section into the same key-indexes that another section has.
     * @param section The section that will be used as a way to get the correct indexes.
     * @param target The target section that will be ordered again.
     */
    private static void correctIndexes(ConfigurationSection section, ConfigurationSection target){
        //Parsing the sections into ArrayLists with their keys and values.
        List<Pair<String, Object>> sectionMap = getSectionMap(section), correctOrder = new ArrayList<>();

        //Going through the sectionMap, which is in the correct order.
        for (Pair<String, Object> entry : sectionMap) {
            //Adding the entry into a new list with the correct value from the target section.
            correctOrder.add(new Pair<>(entry.key, target.get(entry.key)));
        }

        /*The only way to change key-indexes is to add them one-by-one again, in the correct order.
        In order to do so, the section needs to be cleared so the indexes will be reset.*/

        //Clearing the configuration.
        clearConfiguration(target);

        //Adding the entries again in the correct order.
        for(Pair<String, Object> entry : correctOrder)
            target.set(entry.key, entry.value);
    }

    /**
     * Parsing a section into a list that contains all of it's keys and their values without changing their order.
     * @param section The section to convert.
     * @return A list that contains all the keys and their values.
     */
    private static List<Pair<String, Object>> getSectionMap(ConfigurationSection section){
        //Creating an empty ArrayList.
        List<Pair<String, Object>> list = new ArrayList<>();

        //Going through all the keys and adding them in the same order.
        for(String key : section.getKeys(false)) {
            list.add(new Pair<>(key, section.get(key)));
        }

        return list;
    }

    /**
     * Clear a configuration section from all of it's keys.
     * This can be done by setting all the keys' values to null.
     * @param section The section to clear.
     */
    private static void clearConfiguration(ConfigurationSection section){
        for(String key : section.getKeys(false))
            section.set(key, null);
    }

    /**
     * A class that is used as a way of representing a map's entry (which is not implemented).
     */
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