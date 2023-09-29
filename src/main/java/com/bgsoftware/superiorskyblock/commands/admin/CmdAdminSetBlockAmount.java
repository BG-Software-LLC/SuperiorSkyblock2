package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.BlockPositionArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IntArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.WorldArgumentType;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class CmdAdminSetBlockAmount implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("setblockamount", "setblocksize");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setblockamount";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_BLOCK_AMOUNT.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("world", WorldArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_WORLD))
                .add(CommandArgument.required("block-position", BlockPositionArgumentType.INSTANCE, "x> <y> <z"))
                .add(CommandArgument.required("amount", IntArgumentType.AMOUNT, Message.COMMAND_ARGUMENT_AMOUNT))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        World world = context.getRequiredArgument("world", World.class);
        BlockPosition blockPosition = context.getRequiredArgument("block-position", BlockPosition.class);
        int amount = context.getRequiredArgument("amount", Integer.class);

        Location location = blockPosition.parse(world);

        plugin.getStackedBlocks().setStackedBlock(location.getBlock(), amount);

        String formattedLocation = world.getName() + ", " +
                blockPosition.getX() + ", " +
                blockPosition.getY() + ", " +
                blockPosition.getZ();

        Message.CHANGED_BLOCK_AMOUNT.send(dispatcher, formattedLocation, amount);
    }

}
