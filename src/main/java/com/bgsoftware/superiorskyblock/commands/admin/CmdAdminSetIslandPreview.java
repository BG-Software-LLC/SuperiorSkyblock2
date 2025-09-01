package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdAdminSetIslandPreview implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("setislandpreview", "setschematicpreview");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setislandpreview";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setislandpreview <" + Message.COMMAND_ARGUMENT_SCHEMATIC_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_ISLAND_PREVIEW.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        Schematic schematic = CommandArguments.getSchematic(plugin, sender, args[2]);

        if (schematic == null)
            return;

        Player player = (Player) sender;

        String schematicPreviewLocation;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location location = player.getLocation(wrapper.getHandle());
            schematicPreviewLocation = Serializers.LOCATION_SPACED_SERIALIZER.serialize(location);
        }

        try {
            plugin.getSettings().updateValue("preview-islands." + schematic.getName(), schematicPreviewLocation);
        } catch (Exception error) {
            Log.entering("ENTER", schematicPreviewLocation);
            Log.error(error, "An unexpected error occurred while setting schematic preview:");
        }

        Message.ISLAND_PREVIEW_SET.send(sender, schematic.getName(), schematicPreviewLocation);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length == 3 ? CommandTabCompletes.getSchematics(plugin, args[2]) : Collections.emptyList();
    }

}
