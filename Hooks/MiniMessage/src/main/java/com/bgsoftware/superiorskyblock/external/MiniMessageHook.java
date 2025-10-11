package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.MessagesService;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.MessageContent;
import com.bgsoftware.superiorskyblock.service.message.MessagesServiceImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiniMessageHook {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().tags(StandardTags.defaults()).build();

    private static boolean registered = false;

    private static final MessagesServiceImpl.CustomComponentParser PARSER = new MessagesServiceImpl.CustomComponentParser() {
        @Override
        public Optional<IMessageComponent> parse(YamlConfiguration config, String path) {
            if (!config.isString(path))
                return Optional.empty();

            String content = config.getString(path);
            if (Text.isBlank(content))
                return Optional.empty();

            return parse(content);
        }

        @Override
        public Optional<IMessageComponent> parse(String content) {
            try {
                Component component = MINI_MESSAGE.deserialize(Formatters.COLOR_FORMATTER.format(content));
                return Optional.of(new MiniMessageComponent(component));
            } catch (ParsingException error) {
                return Optional.empty();
            }
        }
    };

    public static void register(SuperiorSkyblockPlugin plugin) {
        if (!registered) {
            MessagesServiceImpl messagesService = (MessagesServiceImpl) plugin.getServices().getService(MessagesService.class);
            messagesService.registerCustomComponentParser(PARSER);
            registered = true;
        }
    }

    private static class MiniMessageComponent implements IMessageComponent {

        private final Component component;
        private final MessageContent content;

        MiniMessageComponent(Component component) {
            this.component = component;
            this.content = findTextComponentContent(component);
        }

        @Override
        public Type getType() {
            return Type.COMPLEX_MESSAGE;
        }

        @Override
        public String getMessage() {
            return this.content.getContent(null).orElse("");
        }

        @Override
        public String getMessage(Object... args) {
            return this.content.getContent(null, args).orElse("");
        }

        @Override
        public void sendMessage(CommandSender sender, Object... args) {
            sender.sendMessage(Translator.translate(this.component, args));
        }

        private static MessageContent findTextComponentContent(Component component) {
            if (component instanceof TextComponent textComponent)
                return MessageContent.parse(textComponent.content());

            for (Component children : component.children()) {
                MessageContent childrenContent = findTextComponentContent(children);
                if (childrenContent != MessageContent.EMPTY)
                    return childrenContent;
            }


            return MessageContent.EMPTY;
        }

    }

    private static class Translator {
        private static final Pattern ARG_PATTERN = Pattern.compile("\\{[0-9]+}");

        static Component translate(Component input, Object... args) {
            Component output = input;
            if (args.length != 0) {
                output = output.replaceText(TextReplacementConfig.builder()
                        .match(ARG_PATTERN)
                        .replacement((result, builder) -> doReplacement(result, args))
                        .build());
                output = translateClickEvent(output, args);
            }
            return translateChildren(output, args);
        }

        private static Component translateChildren(Component input, Object... args) {
            List<Component> children = new LinkedList<>();
            for (Component component : input.children()) {
                Component output = translate(component, args);
                children.add(translateChildren(output, args));
            }
            return input.children(children);
        }

        private static Component translateClickEvent(Component input, Object... args) {
            ClickEvent event = input.clickEvent();
            if (event == null) return input;

            String value = event.value();
            Matcher matcher = ARG_PATTERN.matcher(value);

            String result = matcher.replaceAll(match -> doTextReplacement(match, args));
            return input.clickEvent(ClickEvent.clickEvent(event.action(), result));
        }

        private static Component doReplacement(MatchResult match, Object... args) {
            String group = match.group();
            int index = Integer.parseInt(group.substring(1, group.length() - 1));
            return Component.text(index < args.length ? MessageContent.getArgumentString(args[index]) : group);
        }

        private static String doTextReplacement(MatchResult match, Object... args) {
            return PlainTextComponentSerializer.plainText().serialize(doReplacement(match, args));
        }
    }

}
