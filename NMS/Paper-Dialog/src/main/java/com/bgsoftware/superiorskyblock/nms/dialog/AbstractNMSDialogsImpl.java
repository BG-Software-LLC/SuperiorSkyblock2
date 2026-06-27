package com.bgsoftware.superiorskyblock.nms.dialog;

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
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.ItemDialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.minecraft.network.protocol.common.ClientboundClearDialogPacket;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractNMSDialogsImpl implements NMSDialogs {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ClickCallback.Options SINGLE_USE_OPTIONS = ClickCallback.Options.builder()
            .uses(1)
            .lifetime(Duration.ofHours(1))
            .build();

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
                ActionButton.Builder builder = ActionButton.builder(Component.text(dialogButton.getLabel()));
                buildButtonAction(dialog, templateButton, slot).ifPresent(builder::action);
                buttons.add(builder.build());
            }
            ++slot;
        }

        DialogBase.Builder builder = DialogBase.builder(Component.text(menuLayout.getTitle()))
                .canCloseWithEscape(menuLayout.isCloseWithEscapeAllowed())
                .pause(false)
                .afterAction(DialogBase.DialogAfterAction.CLOSE);

        builder.body(buildBodies(menuLayout));

        DialogBase dialogBase = builder.build();
        DialogType dialogType = buildDialogType(menuLayout.getDialogType(), buttons);

        Dialog backedDialog = Dialog.create(factory -> factory.empty()
                .base(dialogBase).type(dialogType));

        dialog.setHandle(backedDialog);

        return dialog;
    }

    @Override
    public void openDialog(SuperiorPlayer superiorPlayer, DialogWrapper<?> dialog) {
        superiorPlayer.runIfOnline(player -> {
            fireDialogOpenEvent(superiorPlayer, dialog);
            player.showDialog(Objects.requireNonNull((DialogLike) dialog.getHandle()));
        });
    }

    @Override
    public void closeDialog(SuperiorPlayer superiorPlayer, DialogWrapper<?> dialog) {
        superiorPlayer.runIfOnline(player -> {
            fireDialogCloseEvent(superiorPlayer, dialog);
            ((CraftPlayer) player).getHandle().connection.send(ClientboundClearDialogPacket.INSTANCE);
        });
    }

    private Optional<DialogAction> buildButtonAction(DialogWrapper<?> dialog, MenuTemplateButton<?> templateButton, int slot) {
        DialogButtonAction action = templateButton.getButtonDialog().getAction();
        if (action == null)
            return Optional.empty();

        switch (action.getType()) {
            case OPEN_URL:
                return Optional.of(DialogAction.staticAction(ClickEvent.openUrl(action.getActionData())));
            case SUGGEST_COMMAND:
                return Optional.of(DialogAction.staticAction(ClickEvent.suggestCommand(action.getActionData())));
            case RUN_COMMAND:
                // fallthrough, we handle commands with MenuCommands
            case CUSTOM:
                return Optional.of(DialogAction.customClick(
                        (response, audience) -> {
                            fireDialogClickEvent(dialog.getMenuView().getInventoryViewer(), dialog, slot);
                        },
                        SINGLE_USE_OPTIONS
                ));
            default:
                throw new IllegalStateException();
        }
    }

    private static List<DialogBody> buildBodies(DialogMenuLayout<?> menuLayout) {
        List<DialogBody> bodies = new LinkedList<>();
        for (DialogBodyElement bodyElement : menuLayout.getBodyElements()) {
            if (bodyElement instanceof DialogBodyText) {
                bodies.add(DialogBody.plainMessage(Component.text(((DialogBodyText) bodyElement).getText())));
            } else if (bodyElement instanceof DialogBodyItem) {
                ItemStack itemStack = ((DialogBodyItem) bodyElement).getItem().build();
                ItemDialogBody.Builder builder = DialogBody.item(itemStack)
                        .showDecorations(((DialogBodyItem) bodyElement).isShowDecorations())
                        .showTooltip(((DialogBodyItem) bodyElement).isShowTooltip());
                String description = ((DialogBodyItem) bodyElement).getDescription();
                if (description != null)
                    builder.description(DialogBody.plainMessage(Component.text(description)));
                bodies.add(builder.build());
            } else {
                throw new IllegalStateException("bodyElement: " + bodyElement);
            }
        }
        return bodies;
    }

    private static DialogType buildDialogType(DialogMenuType menuType, List<ActionButton> buttons) {
        switch (menuType) {
            case CONFIRMATION:
                if (buttons.size() >= 2)
                    return DialogType.confirmation(buttons.get(0), buttons.get(1));
                break;
            case NOTICE:
                if (!buttons.isEmpty())
                    return DialogType.notice(buttons.get(0));
                break;
            default:
                break;
        }
        return DialogType.multiAction(buttons, null, 1);
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
