package com.bgsoftware.superiorskyblock.module.upgrades.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.collections.Lists;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.BaseCommand;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.NumberArgument;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class CmdAdminSetEffect extends BaseCommand implements IAdminIslandCommand {

    @Override
    protected List<String> aliases() {
        return Lists.singleton("seteffect");
    }

    @Override
    protected String permission() {
        return "superior.admin.seteffect";
    }

    @Override
    protected String usage(java.util.Locale locale) {
        return "admin seteffect <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_EFFECT.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_LEVEL.getMessage(locale) + ">";
    }

    @Override
    protected String description(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_EFFECT.getMessage(locale);
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

        if (effectType == null)
            return;

        NumberArgument<Integer> arguments = CommandArguments.getLevel(sender, args[4]);

        if (!arguments.isSucceed())
            return;

        int level = arguments.getNumber();

        boolean anyIslandChanged = false;

        for (Island island : islands) {
            if (level <= 0) {
                if (plugin.getEventsBus().callIslandRemoveEffectEvent(sender, island, effectType)) {
                    anyIslandChanged = true;
                    island.removePotionEffect(effectType);
                }
            } else {
                EventResult<Integer> eventResult = plugin.getEventsBus().callIslandChangeEffectLevelEvent(sender, island, effectType, level);
                anyIslandChanged |= !eventResult.isCancelled();
                if (!eventResult.isCancelled())
                    island.setPotionEffect(effectType, eventResult.getResult());
            }
        }

        if (!anyIslandChanged)
            return;

        if (islands.size() > 1)
            Message.CHANGED_ISLAND_EFFECT_LEVEL_ALL.send(sender, Formatters.CAPITALIZED_FORMATTER.format(effectType.getName()));
        else if (targetPlayer == null)
            Message.CHANGED_ISLAND_EFFECT_LEVEL_NAME.send(sender, Formatters.CAPITALIZED_FORMATTER.format(effectType.getName()), islands.get(0).getName());
        else
            Message.CHANGED_ISLAND_EFFECT_LEVEL.send(sender, Formatters.CAPITALIZED_FORMATTER.format(effectType.getName()), targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getPotionEffects(args[3]) : Lists.emptyList();
    }

}
