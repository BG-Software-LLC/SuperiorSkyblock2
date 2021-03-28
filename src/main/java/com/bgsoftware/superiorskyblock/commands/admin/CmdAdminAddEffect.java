package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminAddEffect implements IAdminIslandCommand {

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
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_EFFECT.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_LEVEL.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_ADD_EFFECT.getMessage(locale);
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        PotionEffectType potionEffectType = CommandArguments.getPotionEffect(sender, args[3]);

        if(potionEffectType == null)
            return;

        Pair<Integer, Boolean> arguments = CommandArguments.getLevel(sender, args[4]);

        if(!arguments.getValue())
            return;

        int level = arguments.getKey();

        Executor.data(() -> islands.forEach(island -> island.setPotionEffect(potionEffectType, island.getPotionEffectLevel(potionEffectType) + level)));

        if(islands.size() > 1)
            Locale.CHANGED_ISLAND_EFFECT_LEVEL_ALL.send(sender, StringUtils.format(potionEffectType.getName()));
        else if(targetPlayer == null)
            Locale.CHANGED_ISLAND_EFFECT_LEVEL_NAME.send(sender, StringUtils.format(potionEffectType.getName()), islands.get(0).getName());
        else
            Locale.CHANGED_ISLAND_EFFECT_LEVEL.send(sender, StringUtils.format(potionEffectType.getName()), targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getPotionEffects(args[3]) : new ArrayList<>();
    }

}
