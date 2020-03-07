package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.islands.SortingTypes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class IslandsTopHook {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Pattern pattern = Pattern.compile("(.*)%island_top_(.+)%(.*)");

    public abstract void refresh(SortingType sortingType);

    protected boolean hasPlaceholders(String str){
        return pattern.matcher(str).matches();
    }

    public static String parsePlaceholders(String str){
        Matcher matcher = pattern.matcher(str);

        if(!matcher.matches())
            return str;

        String topType = matcher.group(2);
        SortingType sortingType;

        if((matcher = Pattern.compile("worth_(.+)").matcher(topType)).matches()){
            sortingType = SortingTypes.BY_WORTH;
        }
        else if((matcher = Pattern.compile("level_(.+)").matcher(topType)).matches()){
            sortingType = SortingTypes.BY_LEVEL;
        }
        else if((matcher = Pattern.compile("rating_(.+)").matcher(topType)).matches()){
            sortingType = SortingTypes.BY_RATING;
        }
        else if((matcher = Pattern.compile("players_(.+)").matcher(topType)).matches()){
            sortingType = SortingTypes.BY_PLAYERS;
        }
        else{
            throw new NullPointerException("Cannot find valid top type.");
        }

        String matcherValue = matcher.group(1);

        boolean value = false;
        boolean leader = false;

        if((matcher = Pattern.compile("value_(.+)").matcher(matcherValue)).matches()){
            value = true;
            matcherValue = matcher.group(1);
        }

        else if((matcher = Pattern.compile("leader_(.+)").matcher(matcherValue)).matches()){
            leader = true;
            matcherValue = matcher.group(1);
        }

        try {
            int index = Integer.parseInt(matcherValue);
            if (index > 0) {
                Island _island = plugin.getGrid().getIsland(index - 1, sortingType);
                assert _island != null;

                if(value){
                    if(sortingType.equals(SortingTypes.BY_WORTH)){
                        return _island.getWorth().toString();
                    }
                    else if(sortingType.equals(SortingTypes.BY_LEVEL)){
                        return _island.getIslandLevel().toString();
                    }
                    else if(sortingType.equals(SortingTypes.BY_RATING)){
                        return StringUtils.format(_island.getTotalRating());
                    }
                    else if(sortingType.equals(SortingTypes.BY_PLAYERS)){
                        return StringUtils.format(_island.getAllPlayersInside().size());
                    }
                }

                else{
                    return leader || _island.getName().isEmpty() ? _island.getOwner().getName() : _island.getName();
                }
            }
        } catch (IllegalArgumentException ignored) { }

        return "N/A";
    }

}
