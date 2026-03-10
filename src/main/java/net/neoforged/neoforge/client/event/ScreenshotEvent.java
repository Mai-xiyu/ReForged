package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.io.IOException;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a screenshot is taken, but before it is written to disk.
 * If cancelled, the screenshot is not written to disk.
 */
public class ScreenshotEvent extends Event implements ICancellableEvent {
    public static final Component DEFAULT_CANCEL_REASON = Component.literal("Screenshot canceled");

    private final NativeImage image;
    private File screenshotFile;
    @Nullable
    private Component resultMessage = null;

    public ScreenshotEvent(NativeImage image, File screenshotFile) {
        this.image = image;
        this.screenshotFile = screenshotFile;
        try {
            this.screenshotFile = screenshotFile.getCanonicalFile();
        } catch (IOException ignored) {}
    }

    /** Wrapper constructor for EventBusAdapter bridging. */
    public ScreenshotEvent(net.minecraftforge.client.event.ScreenshotEvent forge) {
        this(forge.getImage(), forge.getScreenshotFile());
    }

    public NativeImage getImage() { return image; }
    public File getScreenshotFile() { return screenshotFile; }
    public void setScreenshotFile(File screenshotFile) { this.screenshotFile = screenshotFile; }
    @Nullable
    public Component getResultMessage() { return resultMessage; }
    public void setResultMessage(@Nullable Component resultMessage) { this.resultMessage = resultMessage; }

    public Component getCancelMessage() {
        return getResultMessage() != null ? getResultMessage() : DEFAULT_CANCEL_REASON;
    }
}
