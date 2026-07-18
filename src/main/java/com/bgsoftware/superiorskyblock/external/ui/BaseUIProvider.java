package com.bgsoftware.superiorskyblock.external.ui;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.messages.MessageContent;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;

public abstract class BaseUIProvider implements UIProvider {

    @Override
    public abstract IMessageComponent createActionBarComponent(@Nullable String message);

    @Override
    public IMessageComponent createRawMessageComponent(@Nullable String message) {
        return createComplexMessageComponent(message, null, null, null);
    }

    @Override
    public abstract IMessageComponent createComplexMessageComponent(@Nullable String message, @Nullable String command,
                                                                    @Nullable String suggest, @Nullable String tooltip);

    @Override
    public abstract IMessageComponent createComplexMessageComponent(@Nullable BaseComponent[] components);

    @Override
    public abstract IMessageComponent createTitleComponent(@Nullable String titleMessage, @Nullable String subtitleMessage,
                                                           int fadeIn, int stay, int fadeOut);

    protected static abstract class BaseMessageComponent implements IMessageComponent {

        protected final Type type;
        protected final MessageContent messageContent;

        protected BaseMessageComponent(Type type, @Nullable String content) {
            this.type = type;
            this.messageContent = Text.isBlank(content) ? MessageContent.EMPTY : MessageContent.parse(content);
        }

        @Override
        public final Type getType() {
            return this.type;
        }

        @Override
        public final String getMessage() {
            return this.messageContent.getContent(null).orElse("");
        }

        @Override
        public final String getMessage(Object... args) {
            return this.messageContent.getContent(null, args).orElse("");
        }

        @Override
        public abstract void sendMessage(CommandSender sender, Object... args);

    }

}
