package net.neoforged.neoforge.event;

import java.util.Map;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.apache.maven.artifact.versioning.ArtifactVersion;

/**
 * Fired when mod versions from a saved world do not match the current mod versions.
 */
public class ModMismatchEvent extends Event implements IModBusEvent {
    private final Map<String, MismatchedVersionInfo> versionInfo;
    private boolean handled;

    public ModMismatchEvent(Map<String, MismatchedVersionInfo> versionInfo) {
        this.versionInfo = versionInfo;
    }

    public ModMismatchEvent() {
        this(Map.of());
    }

    public Map<String, MismatchedVersionInfo> getVersionDifferences() { return versionInfo; }
    public void markResolved() { this.handled = true; }
    public boolean wasResolved() { return handled; }

    public record MismatchedVersionInfo(String previousVersion, String currentVersion) {}
}
