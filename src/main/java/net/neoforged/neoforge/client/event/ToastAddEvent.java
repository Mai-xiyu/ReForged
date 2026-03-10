package net.neoforged.neoforge.client.event;

import net.minecraft.client.gui.components.toasts.Toast;

/**
 * Fired when a toast notification is about to be added. Cancellable to prevent the toast from appearing.
 */
public class ToastAddEvent extends net.neoforged.bus.api.Event implements net.neoforged.bus.api.ICancellableEvent {
    private final Toast toast;

    public ToastAddEvent(Toast toast) {
        this.toast = toast;
    }

    public Toast getToast() { return toast; }
}
