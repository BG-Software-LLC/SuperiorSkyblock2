package com.bgsoftware.superiorskyblock.core.menu.layout;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogBodyElement;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogButton;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogButtonAction;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogMenuType;
import com.bgsoftware.superiorskyblock.api.menu.layout.DialogMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.core.Either;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.DummyButton;
import com.bgsoftware.superiorskyblock.core.menu.dialog.DialogWrapper;
import com.google.common.base.Preconditions;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RegularDialogMenuLayoutImpl<V extends MenuView<V, ?>> extends AbstractMenuLayout<V> implements DialogMenuLayout<V> {

    private final DialogMenuType dialogMenuType;
    private final boolean closeWithEscapeAllowed;
    private final List<DialogBodyElement> bodyElements;

    protected RegularDialogMenuLayoutImpl(AbstractBuilder<V, ?> builder) {
        super(builder);
        this.dialogMenuType = builder.dialogMenuType;
        this.closeWithEscapeAllowed = builder.closeWithEscapeAllowed;
        this.bodyElements = Collections.unmodifiableList(new LinkedList<>(builder.bodyElements));
    }

    @Override
    public DialogMenuType getDialogType() {
        return this.dialogMenuType;
    }

    @Override
    public boolean isCloseWithEscapeAllowed() {
        return this.closeWithEscapeAllowed;
    }

    @Override
    public List<DialogBodyElement> getBodyElements() {
        return this.bodyElements;
    }

    @Override
    public int getRowsCount() {
        throw new IllegalStateException("Called MenuLayout#getRowsCount on DialogMenuLayout");
    }

    @Override
    public final Inventory buildInventory(V menuView) {
        throw new IllegalStateException("Called MenuLayout#buildInventory on DialogMenuLayout");
    }

    public DialogWrapper<V> buildDialog(V menuView) {
        return plugin.getNMSDialogs()
                .map(nmsDialogs -> nmsDialogs.createDialog(menuView))
                .orElseThrow(() -> new UnsupportedOperationException("Dialogs are not supported"));
    }

    protected static abstract class AbstractBuilder<V extends MenuView<V, ?>, B extends AbstractBuilder<V, B>> extends AbstractMenuLayout.AbstractBuilder<V, B> {

        protected DialogMenuType dialogMenuType = DialogMenuType.MULTI_ACTION;
        protected boolean closeWithEscapeAllowed = false;
        protected final List<DialogBodyElement> bodyElements = new LinkedList<>();

        private boolean dynamicButtonsCount = false;

        public B setDialogType(DialogMenuType dialogMenuType) {
            Preconditions.checkNotNull(dialogMenuType, "dialogMenuType parameter cannot be null");
            this.dialogMenuType = dialogMenuType;
            updateButtons();
            return (B) this;
        }

        public B setCloseWithEscapeAllowed(boolean closeWithEscapeAllowed) {
            this.closeWithEscapeAllowed = closeWithEscapeAllowed;
            return (B) this;
        }

        public B addBodyElement(DialogBodyElement bodyElement) {
            Preconditions.checkNotNull(bodyElement, "bodyElement parameter cannot be null");
            this.bodyElements.add(bodyElement);
            return (B) this;
        }

        public B setInventoryType(InventoryType inventoryType) {
            throw new IllegalStateException("Called MenuLayout.Builder#setInventoryType on DialogMenuLayout.Builder");
        }

        public B setRowsCount(int rowsCount) {
            throw new IllegalStateException("Called MenuLayout.Builder#setRowsCount on DialogMenuLayout.Builder");
        }

        @Override
        public B setButton(int slot, MenuTemplateButton<V> button) {
            ensureButtonsCapacity(slot);
            return super.setButton(slot, button);
        }

        @Override
        public B setButtons(List<Integer> slots, MenuTemplateButton<V> button) {
            for (int slot : slots) {
                ensureButtonsCapacity(slot);
            }

            return super.setButtons(slots, button);
        }

        @Override
        public B mapButton(int slot, MenuTemplateButton.Builder<V> buttonBuilder) {
            ensureButtonsCapacity(slot);

            if (this.buttons[slot] != null) {
                ((AbstractMenuTemplateButton<V>) this.buttons[slot]).applyToBuilder(buttonBuilder);
            }

            setCustomActionIfNeeded(buttonBuilder);

            this.buttons[slot] = buttonBuilder.build();

            return (B) this;
        }

        private void setCustomActionIfNeeded(MenuTemplateButton.Builder<V> buttonBuilder) {
            Either<TemplateItem, DialogButton> buttonData =
                    ((AbstractMenuTemplateButton.AbstractBuilder<V>) buttonBuilder).getButtonData();
            if (buttonData != null) {
                buttonData.ifRight(dialogButton -> {
                    buttonBuilder.setButtonDialog(DialogButton.newBuilder()
                            .setLabel(dialogButton.getLabel())
                            .setAction(DialogButtonAction.Type.CUSTOM, null)
                            .build());
                });
            }
        }

        private void updateButtons() {
            this.dynamicButtonsCount = false;

            switch (dialogMenuType) {
                case CONFIRMATION:
                    this.buttons = new MenuTemplateButton[2];
                    break;
                case NOTICE:
                    this.buttons = new MenuTemplateButton[1];
                    break;
                default:
                    this.dynamicButtonsCount = true;
                    this.buttons = new MenuTemplateButton[0];
                    break;
            }

            Arrays.fill(this.buttons, DummyButton.EMPTY_BUTTON);
        }

        private void ensureButtonsCapacity(int slot) {
            if (this.dynamicButtonsCount && slot >= this.buttons.length)
                this.buttons = Arrays.copyOf(this.buttons, slot + 1);
        }

    }

    public static class Builder<V extends MenuView<V, ?>> extends AbstractBuilder<V, Builder<V>> implements DialogMenuLayout.Builder<V> {

        @Override
        public RegularDialogMenuLayoutImpl<V> build() {
            return new RegularDialogMenuLayoutImpl<>(this);
        }

    }

}
