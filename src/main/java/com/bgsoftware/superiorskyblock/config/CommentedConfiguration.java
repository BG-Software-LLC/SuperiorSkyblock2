package com.bgsoftware.superiorskyblock.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CommentedConfiguration extends YamlConfiguration{

    private Class commentsClass;
    private String[] ignoredSections = new String[] {"ladder", "commands-cooldown"};

    public CommentedConfiguration(Class commentsClass, File file){
        this.commentsClass = commentsClass;
        load(file);
    }

    public void resetYamlFile(Plugin plugin, String resourceName){
        File configFile = new File(plugin.getDataFolder(), resourceName);
        plugin.saveResource(resourceName, true);
        CommentedConfiguration destination = new CommentedConfiguration(commentsClass, configFile);

        copyConfigurationSection(getConfigurationSection(""), destination.getConfigurationSection(""));

        destination.save(configFile);
        load(configFile);
    }

    private void copyConfigurationSection(ConfigurationSection source, ConfigurationSection dest){
        for(String key : dest.getKeys(false)){
            if(source.contains(key)) {
                if (source.isConfigurationSection(key) && (!Arrays.asList(ignoredSections).contains(key) || !dest.contains(key))) {
                    copyConfigurationSection(source.getConfigurationSection(key), dest.getConfigurationSection(key));
                } else {
                    dest.set(key, source.get(key));
                }
            }
        }
    }

    @Override
    public void load(File file){
        try {
            super.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void save(String file){
        save(new File(file));
    }

    @Override
    public void save(File file){
        Map<String, String> comments = new HashMap<>();
        init(comments);

        boolean saved = true;

        // Save the config just like normal
        try {
            super.save(file);
        } catch (Exception e) {
            saved = false;
        }

        // if there's comments to add and it saved fine, we need to add comments
        if (!comments.isEmpty() && saved) {
            // String array of each line in the config file
            String[] yamlContents = convertFileToString(file).split("[" + System.getProperty("line.separator") + "]");

            // This will hold the newly formatted line
            StringBuilder newContents = new StringBuilder();
            // This holds the current path the lines are at in the config
            String currentPath = "";
            // This flags if the line is a node or unknown text.
            boolean node;
            // The depth of the path. (number of words separated by periods - 1)
            int depth = 0;

            if(comments.containsKey("")){
                newContents.append(comments.get("")).append(System.getProperty("line.separator"));
            }

            // Loop through the config lines
            for (String line : yamlContents) {
                if(line.startsWith("#"))
                    continue;

                // If the line is a node (and not something like a list value)
                if (line.contains(": ") || (line.length() > 1 && line.charAt(line.length() - 1) == ':')) {

                    // This is a node so flag it as one
                    node = true;

                    // Grab the index of the end of the node name
                    int index = line.indexOf(": ");
                    if (index < 0) {
                        index = line.length() - 1;
                    }
                    // If currentPath is empty, store the node name as the currentPath. (this is only on the first iteration, i think)
                    if (currentPath.isEmpty()) {
                        currentPath = line.substring(0, index);
                    } else {
                        // Calculate the whitespace preceding the node name
                        int whiteSpace = 0;
                        for (int n = 0; n < line.length(); n++) {
                            if (line.charAt(n) == ' ') {
                                whiteSpace++;
                            } else {
                                break;
                            }
                        }
                        // Find out if the current depth (whitespace * 2) is greater/lesser/equal to the previous depth
                        if (whiteSpace / 2 > depth) {
                            // Path is deeper.  Add a . and the node name
                            currentPath += "." + line.substring(whiteSpace, index);
                            depth++;
                        } else if (whiteSpace / 2 < depth) {
                            // Path is shallower, calculate current depth from whitespace (whitespace / 2) and subtract that many levels from the currentPath
                            int newDepth = whiteSpace / 2;
                            for (int i = 0; i < depth - newDepth; i++) {
                                currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                            }
                            // Grab the index of the final period
                            int lastIndex = currentPath.lastIndexOf(".");
                            if (lastIndex < 0) {
                                // if there isn't a final period, set the current path to nothing because we're at root
                                currentPath = "";
                            } else {
                                // If there is a final period, replace everything after it with nothing
                                currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                                currentPath += ".";
                            }
                            // Add the new node name to the path
                            currentPath += line.substring(whiteSpace, index);
                            // Reset the depth
                            depth = newDepth;
                        } else {
                            // Path is same depth, replace the last path node name to the current node name
                            int lastIndex = currentPath.lastIndexOf(".");
                            if (lastIndex < 0) {
                                // if there isn't a final period, set the current path to nothing because we're at root
                                currentPath = "";
                            } else {
                                // If there is a final period, replace everything after it with nothing
                                currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                                currentPath += ".";
                            }
                            //currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                            currentPath += line.substring(whiteSpace, index);

                        }

                    }

                } else
                    node = false;

                if (node) {
                    String comment = comments.get(currentPath);
                    if (comment != null) {
                        // Add the comment to the beginning of the current line
                        line = comment + System.getProperty("line.separator") + line + System.getProperty("line.separator");
                    } else {
                        // Add a new line as it is a node, but has no comment
                        line += System.getProperty("line.separator");
                    }
                }

                // Add the (modified) line to the total config String
                newContents.append(line).append((!node) ? System.getProperty("line.separator") : "");

            }
            /*
             * Due to a bukkit bug we need to strip any extra new lines from the
             * beginning of this file, else they will multiply.
             */
            while (newContents.toString().startsWith(System.getProperty("line.separator")))
                newContents = new StringBuilder(newContents.toString().replaceFirst(System.getProperty("line.separator"), ""));

            //System.out.println(newContents);

            // Write the string to the config file
            stringToFile(newContents.toString(), file);
        }
    }

    private void init(Map<String, String> comments){
        if(commentsClass == null)
            return;

        try {
            for (Field field : commentsClass.getDeclaredFields()) {
                List<Comment> commentList = new ArrayList<>();

                for (Annotation annotation : field.getAnnotations()) {
                    if (annotation instanceof Comment.List) {
                        commentList.addAll(Arrays.asList(((Comment.List) annotation).value()));
                    } else if (annotation instanceof Comment) {
                        commentList.add((Comment) annotation);
                    }
                }

                if(!commentList.isEmpty()){
                    String pathName = (String) field.get(this);
                    String whiteSpaces = getWhiteSpaces(pathName);
                    StringBuilder comment = new StringBuilder();

                    for (Comment _comment : commentList) {
                        String cmnt = _comment.value();
                        if (cmnt.isEmpty()) {
                            comment.append(System.getProperty("line.separator"));
                        } else {
                            if (pathName.isEmpty()) {
                                comment.append(cmnt).append(System.getProperty("line.separator"));
                            } else {
                                comment.append(whiteSpaces).append("# ").append(cmnt).append(System.getProperty("line.separator"));
                            }
                        }
                    }

                    comments.put(pathName, comment.substring(0, comment.length() - 2));
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private String getWhiteSpaces(String pathName) {
        StringBuilder whiteSpaces = new StringBuilder();

        for(int i = 0; i < pathName.length(); i++){
            if(pathName.charAt(i) == '.'){
                whiteSpaces.append("  ");
            }
        }

        return whiteSpaces.toString();
    }


    private String convertFileToString(File file) {
        if (file != null && file.exists() && file.canRead() && !file.isDirectory()) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try (InputStream is = new FileInputStream(file)) {
                Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
                reader.close();
            } catch (IOException e) {
                System.out.println("Exception ");
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void stringToFile(String source, File file) {
        try {
            if(file.exists())
                file.delete();

            file.getParentFile().mkdirs();
            file.createNewFile();

            //OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));

            out.write(source);
            out.close();
        } catch (IOException e) {
            System.out.println("Exception ");
        }
    }

}
