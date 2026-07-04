package com.bgsoftware.superiorskyblock.nms.v26_2;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogBodyElement;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogButton;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogButtonAction;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogMenuType;
import com.bgsoftware.superiorskyblock.api.menu.layout.DialogMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.dialog.DialogWrapper;
import com.bgsoftware.superiorskyblock.core.menu.dialog.body.DialogBodyItem;
import com.bgsoftware.superiorskyblock.core.menu.dialog.body.DialogBodyText;
import com.bgsoftware.superiorskyblock.nms.NMSDialogs;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundClearDialogPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.ConfirmationDialog;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.MultiActionDialog;
import net.minecraft.server.dialog.NoticeDialog;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.ItemBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.world.item.ItemStackTemplate;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class NMSDialogsImpl implements NMSDialogs {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Identifier DIALOG_CLICK_CALLBACK_KEY =
            Identifier.fromNamespaceAndPath("superiorskyblock", "dialog_click_callback");

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public <V extends MenuView<V, ?>> DialogWrapper<V> createDialog(V menuView) {
        DialogWrapper<V> dialog = new DialogWrapper<>(menuView);

        DialogMenuLayout<V> menuLayout = (DialogMenuLayout<V>) menuView.getMenu().getLayout();

        List<ActionButton> buttons = new LinkedList<>();
        int slot = 0;
        for (MenuTemplateButton<?> templateButton : menuLayout.getButtons()) {
            DialogButton dialogButton = templateButton.getButtonDialog();
            if (dialogButton != null) {
                CommonButtonData buttonData = new CommonButtonData(Component.literal(dialogButton.getLabel()), dialogButton.getWidth());
                Optional<Action> action = buildButtonAction(dialog, templateButton, slot);
                buttons.add(new ActionButton(buttonData, action));
            }
            ++slot;
        }

        CommonDialogData dialogData = new CommonDialogData(
                Component.literal(menuLayout.getTitle()),
                Optional.empty(), // TODO
                menuLayout.isCloseWithEscapeAllowed(),
                false, // TODO
                DialogAction.CLOSE,
                buildBodies(menuLayout),
                Collections.emptyList()
        );

        Dialog backedDialog = buildDialog(menuLayout.getDialogType(), dialogData, buttons);

        dialog.setHandle(backedDialog);

        return dialog;
    }

    @Override
    public void openDialog(SuperiorPlayer superiorPlayer, DialogWrapper<?> dialog) {
        superiorPlayer.runIfOnline(player -> {
            fireDialogOpenEvent(superiorPlayer, dialog);
            ((CraftPlayer) player).getHandle().openDialog(Holder.direct((Dialog) dialog.getHandle()));
            // TODO - handle custom actions?
        });
    }

    @Override
    public void closeDialog(SuperiorPlayer superiorPlayer, DialogWrapper<?> dialog) {
        superiorPlayer.runIfOnline(player -> {
            fireDialogCloseEvent(superiorPlayer, dialog);
            ((CraftPlayer) player).getHandle().connection.send(ClientboundClearDialogPacket.INSTANCE);
        });
    }

    private Optional<Action> buildButtonAction(DialogWrapper<?> dialog, MenuTemplateButton<?> templateButton, int slot) {
        DialogButtonAction action = templateButton.getButtonDialog().getAction();
        if (action == null)
            return Optional.empty();

        switch (action.getType()) {
            case OPEN_URL:
                try {
                    return Optional.of(new StaticAction(new ClickEvent.OpenUrl(new URI(action.getActionData()))));
                } catch (URISyntaxException error) {
                    return Optional.empty();
                }
            case SUGGEST_COMMAND:
                return Optional.of(new StaticAction(new ClickEvent.SuggestCommand(action.getActionData())));
            case RUN_COMMAND:
                // fallthrough, we handle commands with MenuCommands
            case CUSTOM:
                int id = dialog.registerCallback(() ->
                        fireDialogClickEvent(dialog.getMenuView().getInventoryViewer(), dialog, slot));
                CompoundTag tag = new CompoundTag();
                tag.putInt("id", id);
                return Optional.of(new StaticAction(new ClickEvent.Custom(DIALOG_CLICK_CALLBACK_KEY, Optional.of(tag))));
            default:
                throw new IllegalStateException();
        }
    }

    private static List<DialogBody> buildBodies(DialogMenuLayout<?> menuLayout) {
        List<DialogBody> bodies = new LinkedList<>();
        for (DialogBodyElement bodyElement : menuLayout.getBodyElements()) {
            if (bodyElement instanceof DialogBodyText) {
                bodies.add(getPlainMessage(bodyElement));
            } else if (bodyElement instanceof DialogBodyItem) {
                bodies.add(getItemBody(bodyElement));
            } else {
                throw new IllegalStateException("bodyElement: " + bodyElement);
            }
        }
        return bodies;
    }

    private static PlainMessage getPlainMessage(DialogBodyElement dialogBodyElement) {
        return (PlainMessage) ((DialogBodyText) dialogBodyElement).getNMSHandle(dialogBodyText ->
                new PlainMessage(Component.literal(dialogBodyText.getText()), dialogBodyText.getWidth()));
    }

    private static ItemBody getItemBody(DialogBodyElement dialogBodyElement) {
        return (ItemBody) ((DialogBodyItem) dialogBodyElement).getNMSHandle(dialogBodyItem -> {
            ItemStack itemStack = dialogBodyItem.getItem().build();
            ItemStackTemplate itemStackTemplate = ItemStackTemplate.fromNonEmptyStack(CraftItemStack.asNMSCopy(itemStack));
            Optional<PlainMessage> description = Optional.ofNullable(dialogBodyItem.getDescription()).map(NMSDialogsImpl::getPlainMessage);
            return new ItemBody(itemStackTemplate, description, dialogBodyItem.isShowDecorations(),
                    dialogBodyItem.isShowTooltip(), dialogBodyItem.getWidth(), dialogBodyItem.getHeight());
        });
    }

    private static Dialog buildDialog(DialogMenuType menuType, CommonDialogData dialogData, List<ActionButton> buttons) {
        switch (menuType) {
            case CONFIRMATION:
                if (buttons.size() >= 2)
                    return new ConfirmationDialog(dialogData, buttons.get(0), buttons.get(1));
                break;
            case NOTICE:
                if (!buttons.isEmpty())
                    return new NoticeDialog(dialogData, buttons.get(0));
                break;
            default:
                break;
        }
        return new MultiActionDialog(dialogData, buttons, Optional.empty(), 1);
    }

    private static void fireDialogOpenEvent(SuperiorPlayer superiorPlayer, DialogWrapper<?> dialog) {
        GameEventArgs.DialogOpenEvent dialogOpenEvent = new GameEventArgs.DialogOpenEvent();
        dialogOpenEvent.superiorPlayer = superiorPlayer;
        dialogOpenEvent.dialog = dialog;
        GameEvent<GameEventArgs.DialogOpenEvent> gameEvent = new GameEvent<>(GameEventType.DIALOG_OPEN_EVENT, dialogOpenEvent);
        plugin.getGameEventsDispatcher().onGameEvent(gameEvent, GameEventPriority.NORMAL);
    }

    private static void fireDialogClickEvent(SuperiorPlayer superiorPlayer, DialogWrapper<?> dialog, int clickedSlot) {
        GameEventArgs.DialogClickEvent dialogClickEvent = new GameEventArgs.DialogClickEvent();
        dialogClickEvent.superiorPlayer = superiorPlayer;
        dialogClickEvent.dialog = dialog;
        dialogClickEvent.clickedSlot = clickedSlot;
        GameEvent<GameEventArgs.DialogClickEvent> gameEvent = new GameEvent<>(GameEventType.DIALOG_CLICK_EVENT, dialogClickEvent);
        plugin.getGameEventsDispatcher().onGameEvent(gameEvent, GameEventPriority.NORMAL);
    }

    private static void fireDialogCloseEvent(SuperiorPlayer superiorPlayer, DialogWrapper<?> dialog) {
        GameEventArgs.DialogCloseEvent dialogCloseEvent = new GameEventArgs.DialogCloseEvent();
        dialogCloseEvent.superiorPlayer = superiorPlayer;
        dialogCloseEvent.dialog = dialog;
        GameEvent<GameEventArgs.DialogCloseEvent> gameEvent = new GameEvent<>(GameEventType.DIALOG_CLOSE_EVENT, dialogCloseEvent);
        plugin.getGameEventsDispatcher().onGameEvent(gameEvent, GameEventPriority.NORMAL);
    }

}
