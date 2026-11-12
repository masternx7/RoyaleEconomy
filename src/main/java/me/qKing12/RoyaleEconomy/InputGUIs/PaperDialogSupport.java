package me.qKing12.RoyaleEconomy.InputGUIs;

/**
 * Checks whether the Paper Dialog API is available on the running server.
 *
 * This class intentionally does not reference any dialog class directly so it
 * can be loaded safely on Spigot / older Paper versions.
 */
public final class PaperDialogSupport {

    private static Boolean available;

    private PaperDialogSupport() {
    }

    public static boolean isAvailable() {
        if (available == null) {
            try {
                Class.forName("io.papermc.paper.dialog.Dialog");
                Class.forName("io.papermc.paper.dialog.DialogResponseView");
                Class.forName("io.papermc.paper.registry.data.dialog.ActionButton");
                Class.forName("io.papermc.paper.registry.data.dialog.DialogBase");
                Class.forName("io.papermc.paper.registry.data.dialog.body.DialogBody");
                Class.forName("io.papermc.paper.registry.data.dialog.input.DialogInput");
                Class.forName("io.papermc.paper.registry.data.dialog.type.DialogType");
                Class.forName("io.papermc.paper.registry.data.dialog.action.DialogAction");
                available = true;
            } catch (Throwable ignored) {
                available = false;
            }
        }
        return available;
    }
}
