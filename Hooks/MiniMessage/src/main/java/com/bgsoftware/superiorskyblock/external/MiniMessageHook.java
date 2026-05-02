package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.MessagesService;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.MessageContent;
import com.bgsoftware.superiorskyblock.service.message.MessagesServiceImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Optional;

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
                return Optional.of(new MiniMessageComponent(component, content));
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

        MiniMessageComponent(Component component, String content) {
            this.component = component;
            this.content = MessageContent.parse(content);
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
            if (args.length == 0) {
                sender.sendMessage(this.component);
            } else {
                Player player = sender instanceof Player ? (Player) sender : null;

                this.content.getContent(player, args).ifPresent(message -> {
                    Component finalComponent = MINI_MESSAGE.deserialize(Formatters.COLOR_FORMATTER.format(message));
                    sender.sendMessage(finalComponent);
                });
            }
        }

    }

}
