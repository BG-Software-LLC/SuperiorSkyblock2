package com.bgsoftware.superiorskyblock.module.upgrades.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.BulkChange;
import com.bgsoftware.superiorskyblock.commands.ChangeResult;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.NumberArgument;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

public class CmdAdminAddEffect implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addeffect");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addeffect";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin addeffect <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_EFFECT.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_LEVEL.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_ADD_EFFECT.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 5;
    }

    @Override
    public int getMaxArgs() {
        return 5;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean supportMultipleIslands() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        PotionEffectType effectType = CommandArguments.getPotionEffect(sender, args[3]);

        if (effectType == null) {
            return;
        }

        NumberArgument<Integer> arguments = CommandArguments.getLevel(sender, args[4]);

        if (!arguments.isSucceed()) {
            return;
        }

        int level = arguments.getNumber();

        if (level == 0) {
            Message.INVALID_LEVEL.send(sender, level);
            return;
        }

        BulkChange<Island> change = BulkChange.ofIslands(islands, island -> {
            int newLevel = island.getPotionEffectLevel(effectType) + level;

            if (newLevel < 0) {
                return ChangeResult.BELOW_MINIMUM;
            }

            return ChangeResult.ifAllowed(PluginEventsFactory.callIslandChangeEffectLevelEvent(island, sender, effectType, newLevel),
                    eventArgs -> eventArgs.island.setPotionEffect(eventArgs.effectType, eventArgs.effectLevel));
        });

        change.sendSummary(sender, args);
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getPotionEffects(args[3]) : Collections.emptyList();
    }

}
