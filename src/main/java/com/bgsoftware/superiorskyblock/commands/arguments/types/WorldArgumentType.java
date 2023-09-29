package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldArgumentType implements CommandArgumentType<World> {

    public static final WorldArgumentType INSTANCE = new WorldArgumentType();

    @Override
    public World parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String name = reader.read();

        World world = Bukkit.getWorld(name);

        if (world == null) {
            Message.INVALID_WORLD.send(context.getDispatcher(), name);
            throw new CommandSyntaxException("Invalid world: " + name);
        }

        return world;
    }

}
