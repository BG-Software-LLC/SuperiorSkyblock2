package com.bgsoftware.superiorskyblock.core.messages;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageContent {

    public static final MessageContent EMPTY = new MessageContent(Collections.emptyList()) {
        @Override
        public Optional<String> getContent(@Nullable OfflinePlayer unused, Object... unused2) {
            return Optional.empty();
        }
    };

    private static final LazyReference<PlaceholdersService> placeholdersService = new LazyReference<PlaceholdersService>() {
        @Override
        protected PlaceholdersService create() {
            return SuperiorSkyblockPlugin.getPlugin().getServices().getService(PlaceholdersService.class);
        }
    };

    private static final Pattern DEFAULT_PLACEHOLDER_PATTERN = Pattern.compile("\\{(\\d+)}");

    private final List<IPart> contentParts = new LinkedList<>();

    public static List<MessageContent> parse(List<String> contents) {
        List<MessageContent> messageContentsList = new LinkedList<>();
        for (String content : contents)
            messageContentsList.add(parse(content));
        return messageContentsList;
    }

    public static MessageContent parse(String content) {
        Matcher matcher = DEFAULT_PLACEHOLDER_PATTERN.matcher(content);

        List<IPart> parts = new LinkedList<>();
        int lastPartIdx = 0;

        while (matcher.find()) {
            StringBuilder previousPart = new StringBuilder(content.substring(lastPartIdx, matcher.start()));

            ArgumentPart argumentPart = null;

            try {
                int argumentIndex = Integer.parseInt(matcher.group(1));
                argumentPart = ArgumentPart.fromIndex(argumentIndex);
            } catch (NumberFormatException error) {
                previousPart.append(matcher.group());
            }

            if (previousPart.length() > 0)
                parts.add(new StaticPart(previousPart.toString()));
            if (argumentPart != null)
                parts.add(argumentPart);

            lastPartIdx = matcher.end();
        }

        if (lastPartIdx < content.length())
            parts.add(new StaticPart(content.substring(lastPartIdx)));

        return new MessageContent(parts);
    }

    private MessageContent(List<IPart> contentParts) {
        this.contentParts.addAll(contentParts);
    }

    public Optional<String> getContent(@Nullable OfflinePlayer offlinePlayer, Object... arguments) {
        StringBuilder content = new StringBuilder();
        for (IPart part : contentParts) {
            if (part instanceof StaticPart) {
                content.append(((StaticPart) part).content);
            } else {
                int argumentIndex = ((ArgumentPart) part).argumentIndex;
                if (argumentIndex >= 0 && argumentIndex < arguments.length) {
                    content.append(getArgumentString(arguments[argumentIndex]));
                } else {
                    content.append("{").append(argumentIndex).append("}");
                }
            }
        }

        if (content.length() == 0)
            return Optional.empty();

        return Optional.of(placeholdersService.get().parsePlaceholders(offlinePlayer, content.toString()));
    }

    public static String getArgumentString(Object argument) {
        return argument instanceof BigDecimal ?
                Formatters.NUMBER_FORMATTER.format((BigDecimal) argument) :
                String.valueOf(argument);
    }

    private interface IPart {

    }

    private static class StaticPart implements IPart {

        private final String content;

        StaticPart(String content) {
            this.content = content;
        }

    }

    private static class ArgumentPart implements IPart {

        private static final ArgumentPart[] CACHED_PARTS = new ArgumentPart[]{
                new ArgumentPart(0),
                new ArgumentPart(1),
                new ArgumentPart(2),
                new ArgumentPart(3)
        };

        private final int argumentIndex;

        static ArgumentPart fromIndex(int index) {
            return index >= 0 && index < CACHED_PARTS.length ? CACHED_PARTS[index] : new ArgumentPart(index);
        }

        ArgumentPart(int argumentIndex) {
            this.argumentIndex = argumentIndex;
        }

    }

}
