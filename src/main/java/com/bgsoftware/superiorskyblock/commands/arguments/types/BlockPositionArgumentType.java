package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.core.SBlockPosition;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class BlockPositionArgumentType implements CommandArgumentType<BlockPosition> {

    public static final BlockPositionArgumentType INSTANCE = new BlockPositionArgumentType();

    @Override
    public BlockPosition parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String x = reader.read();
        String y = reader.read();
        String z = reader.read();

        try {
            return new SBlockPosition(null, Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z));
        } catch (Throwable ex) {
            String formattedBlockPosition = x + ", " + y + ", " + z;
            Message.INVALID_BLOCK.send(context.getDispatcher(), formattedBlockPosition);
            throw new CommandSyntaxException("Invalid block position: " + formattedBlockPosition);
        }
    }

    @Override
    public List<String> getSuggestions(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) {
        String suggestion = null;

        Location location = ((Player) context.getDispatcher()).getLocation();

        String block;

        try {
            block = location.getBlockX() + "";
            if (block.contains(reader.read()))
                suggestion = block;

            block = location.getBlockY() + "";
            if (block.contains(reader.read()))
                suggestion = block;

            block = location.getBlockZ() + "";
            if (block.contains(reader.read()))
                suggestion = block;
        } catch (CommandSyntaxException ignored) {
        }

        return suggestion == null ? Collections.emptyList() : Collections.singletonList(suggestion);
    }

}
