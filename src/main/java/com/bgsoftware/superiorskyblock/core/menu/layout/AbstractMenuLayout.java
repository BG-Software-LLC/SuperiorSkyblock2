package com.bgsoftware.superiorskyblock.core.menu.layout;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.DummyButton;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractMenuView;
import com.bgsoftware.superiorskyblock.core.messages.MessageContent;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class AbstractMenuLayout<V extends MenuView<V, ?>> implements MenuLayout<V> {

    private static final Object[] EMPTY_ARGS = new Object[0];

    public static final char[] BUTTON_SYMBOLS = new char[]{
            '!', '@', '#', '$', '%', '^', '&', '*', '-', '_', '+', '=',
            '~', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '>',
            '<', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
            'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
            'X', 'Y', 'Z'
    };

    protected static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    protected final MessageContent title;
    protected final String rawTitle;
    protected final MenuTemplateButton<V>[] buttons;

    protected AbstractMenuLayout(AbstractBuilder<V, ?> builder) {
        this.rawTitle = builder.title.replace("{}", "{0}"); // {} -> {0} so it works with MessageContent
        this.title = MessageContent.parse(this.rawTitle);
        this.buttons = builder.buttons;
    }

    @Override
    public String getTitle() {
        return this.rawTitle;
    }

    public String getTitle(V menuView) {
        Object[] titleArgs = getMenuViewTitleArgs(menuView);
        return this.title.getContent(menuView.getInventoryViewer().asOfflinePlayer(), titleArgs).orElse("");
    }

    @Override
    public final MenuTemplateButton<V> getButton(int slot) {
        return slot < 0 || slot >= this.buttons.length ? DummyButton.EMPTY_BUTTON : this.buttons[slot];
    }

    @Override
    public Collection<MenuTemplateButton<V>> getButtons() {
        return new SequentialListBuilder<MenuTemplateButton<V>>().build(Arrays.asList(buttons));
    }

    private static Object[] getMenuViewTitleArgs(MenuView<?, ?> menuView) {
        if (menuView instanceof AbstractMenuView) {
            Object[] titleArgs = ((AbstractMenuView<?, ?>) menuView).getTitleArgs();
            if (titleArgs != null)
                return titleArgs;
        }

        return EMPTY_ARGS;
    }

    @SuppressWarnings("unchecked")
    public static abstract class AbstractBuilder<V extends MenuView<V, ?>, B extends AbstractBuilder<V, B>> {

        protected String title = "";
        protected MenuTemplateButton<V>[] buttons;

        protected AbstractBuilder() {
        }

        public B setTitle(String title) {
            this.title = title;
            return (B) this;
        }

        public B setButton(int slot, MenuTemplateButton<V> button) {
            if (slot >= 0 && slot < this.buttons.length)
                this.buttons[slot] = button;

            return (B) this;
        }

        public B setButtons(MenuTemplateButton<V>[] buttons) {
            for (int slot = 0; slot < this.buttons.length && slot < buttons.length; ++slot)
                this.buttons[slot] = buttons[slot];

            return (B) this;
        }

        public B setButtons(List<Integer> slots, MenuTemplateButton<V> button) {
            for (int slot : slots) {
                if (slot >= 0 && slot < this.buttons.length)
                    this.buttons[slot] = button;
            }
            return (B) this;
        }

        public B mapButton(int slot, MenuTemplateButton.Builder<V> buttonBuilder) {
            if (slot >= 0 && slot < this.buttons.length) {
                if (this.buttons[slot] == null) {
                    this.buttons[slot] = buttonBuilder.build();
                } else {
                    this.buttons[slot] = ((AbstractMenuTemplateButton<V>) this.buttons[slot]).applyToBuilder(buttonBuilder).build();
                }
            }
            return (B) this;
        }

        public B mapButtons(List<Integer> slots, MenuTemplateButton.Builder<V> buttonBuilder) {
            for (int slot : slots) {
                mapButton(slot, buttonBuilder);
            }
            return (B) this;
        }

    }

}
