package net.neoforged.fml;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Proxy: NeoForge FML's ModLoadingIssue — represents a warning or error during mod loading.
 *
 * <p>NeoForge mods (e.g. YSM) may reference this class when reporting deferred loading errors.
 * This stub provides the same API surface so that mod code that catches/creates
 * ModLoadingIssue instances does not crash with ClassNotFoundException.</p>
 */
public final class ModLoadingIssue {

    private static final Logger LOGGER = LogUtils.getLogger();

    public enum Severity {
        WARNING,
        ERROR
    }

    private final Severity severity;
    private final String translationKey;
    private final Object[] translationArgs;
    private final List<Object> affectedMods;
    private final Throwable cause;

    private ModLoadingIssue(Severity severity, String translationKey, Object[] translationArgs,
                            List<Object> affectedMods, Throwable cause) {
        this.severity = severity;
        this.translationKey = translationKey;
        this.translationArgs = translationArgs;
        this.affectedMods = affectedMods;
        this.cause = cause;
    }

    public static ModLoadingIssue error(String translationKey, Object... args) {
        LOGGER.warn("[ReForged] ModLoadingIssue.error: {} {}", translationKey, args);
        return new ModLoadingIssue(Severity.ERROR, translationKey, args, Collections.emptyList(), null);
    }

    public static ModLoadingIssue warning(String translationKey, Object... args) {
        LOGGER.warn("[ReForged] ModLoadingIssue.warning: {} {}", translationKey, args);
        return new ModLoadingIssue(Severity.WARNING, translationKey, args, Collections.emptyList(), null);
    }

    public ModLoadingIssue withAffectedMod(Object modInfo) {
        List<Object> newMods = new ArrayList<>(this.affectedMods);
        newMods.add(modInfo);
        return new ModLoadingIssue(this.severity, this.translationKey, this.translationArgs, newMods, this.cause);
    }

    /** Typed overload matching NeoForge's exact method descriptor for IModInfo */
    public ModLoadingIssue withAffectedMod(IModInfo modInfo) {
        return withAffectedMod((Object) modInfo);
    }

    public ModLoadingIssue withAffectedMods(List<?> modInfos) {
        List<Object> newMods = new ArrayList<>(this.affectedMods);
        newMods.addAll(modInfos);
        return new ModLoadingIssue(this.severity, this.translationKey, this.translationArgs, newMods, this.cause);
    }

    public ModLoadingIssue withCause(Throwable cause) {
        return new ModLoadingIssue(this.severity, this.translationKey, this.translationArgs, this.affectedMods, cause);
    }

    public Severity severity() { return severity; }
    public String translationKey() { return translationKey; }
    public Object[] translationArgs() { return translationArgs; }
    public List<Object> affectedMods() { return affectedMods; }
    public Optional<Throwable> cause() { return Optional.ofNullable(cause); }

    @Override
    public String toString() {
        return "ModLoadingIssue[" + severity + ": " + translationKey + "]";
    }
}
