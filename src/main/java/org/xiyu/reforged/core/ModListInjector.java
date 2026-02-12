package org.xiyu.reforged.core;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IConfigurable;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

/**
 * Injects NeoForge mod entries into Forge's {@link ModList} via reflection,
 * so they appear in the Mods screen and are recognized by {@code ModList.isLoaded()}.
 *
 * <p>Forge's ModList uses immutable collections internally, so we create new
 * copies with the additional entries and replace the fields atomically.</p>
 */
public final class ModListInjector {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Metadata for a loaded NeoForge mod to be registered with Forge.
     */
    public record NeoModData(String modId, String displayName, String version,
                              String description, String license, String logoFile,
                              Object modInstance) {}

    /**
     * Create a {@link NeoModContainer} (Forge ModContainer subclass) from a {@link NeoModData}.
     * The container is created with a null mod instance; call {@link NeoModContainer#setModInstance(Object)}
     * after the mod is constructed.
     *
     * <p>This is used before mod construction so that Forge's {@code ModLoadingContext.setActiveContainer()}
     * can be called with the correct container, ensuring configs register under the correct mod ID.</p>
     */
    public static NeoModContainer createContainer(NeoModData data) {
        NeoModFileInfo fileInfo = new NeoModFileInfo(data);
        NeoModInfo modInfo = new NeoModInfo(data, fileInfo);
        fileInfo.setModInfo(modInfo);
        return new NeoModContainer(modInfo, null);
    }

    /**
     * Inject pre-created NeoForge mod containers into Forge's ModList.
     * Must be called after ModList is initialized (during or after @Mod construction).
     */
    public static void inject(List<NeoModContainer> containers) {
        if (containers.isEmpty()) return;

        try {
            ModList modList = ModList.get();
            if (modList == null) {
                LOGGER.warn("[ReForged] ModList not yet available, cannot register NeoForge mods");
                return;
            }

            // Reflect into ModList's private immutable fields
            Field modsField = ModList.class.getDeclaredField("mods");
            Field indexedModsField = ModList.class.getDeclaredField("indexedMods");
            Field sortedContainersField = ModList.class.getDeclaredField("sortedContainers");
            Field sortedListField = ModList.class.getDeclaredField("sortedList");
            Field scanDataField = ModList.class.getDeclaredField("modFileScanData");

            modsField.setAccessible(true);
            indexedModsField.setAccessible(true);
            sortedContainersField.setAccessible(true);
            sortedListField.setAccessible(true);
            scanDataField.setAccessible(true);

            @SuppressWarnings("unchecked")
            List<ModContainer> existingMods =
                    (List<ModContainer>) modsField.get(modList);
            @SuppressWarnings("unchecked")
            Map<String, ModContainer> existingIndexed =
                    (Map<String, ModContainer>) indexedModsField.get(modList);
            @SuppressWarnings("unchecked")
            List<ModContainer> existingSorted =
                    (List<ModContainer>) sortedContainersField.get(modList);
            @SuppressWarnings("unchecked")
            List<IModInfo> existingSortedList =
                    (List<IModInfo>) sortedListField.get(modList);

            // Force modFileScanData to be populated BEFORE we modify sortedList.
            // getAllScanData() lazily iterates sortedList; if we add our entries first,
            // it would NPE on getOwningFile().getFile().getScanResult().
            // By calling it now, the cache is filled with only real Forge mods.
            modList.getAllScanData();

            // Create mutable copies (originals are unmodifiable)
            List<ModContainer> newMods = new ArrayList<>(existingMods);
            Map<String, ModContainer> newIndexed = new HashMap<>(existingIndexed);
            List<ModContainer> newSorted = new ArrayList<>(existingSorted);
            List<IModInfo> newSortedList = new ArrayList<>(existingSortedList);

            for (NeoModContainer container : containers) {
                IModInfo modInfo = container.getModInfo();

                newMods.add(container);
                newIndexed.put(container.getModId(), container);
                newSorted.add(container);
                newSortedList.add(modInfo);

                LOGGER.info("[ReForged] Registered '{}' in Forge mod list", container.getModId());
            }

            // Replace fields atomically
            modsField.set(modList, Collections.unmodifiableList(newMods));
            indexedModsField.set(modList, Collections.unmodifiableMap(newIndexed));
            sortedContainersField.set(modList, Collections.unmodifiableList(newSorted));
            sortedListField.set(modList, Collections.unmodifiableList(newSortedList));
            // modFileScanData is already cached (we called getAllScanData() above),
            // so it won't re-iterate sortedList and NPE on our entries.

        } catch (Exception e) {
            LOGGER.error("[ReForged] Failed to inject NeoForge mods into Forge ModList", e);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  IModInfo implementation
    // ═══════════════════════════════════════════════════════════

    static class NeoModInfo implements IModInfo, IConfigurable {

        private final NeoModData data;
        private final NeoModFileInfo fileInfo;
        private final ArtifactVersion version;

        NeoModInfo(NeoModData data, NeoModFileInfo fileInfo) {
            this.data = data;
            this.fileInfo = fileInfo;
            this.version = new DefaultArtifactVersion(data.version());
        }

        @Override public IModFileInfo getOwningFile() { return fileInfo; }
        @Override public String getModId() { return data.modId(); }
        @Override public String getDisplayName() { return data.displayName(); }
        @Override public String getDescription() { return data.description() != null ? data.description() : ""; }
        @Override public ArtifactVersion getVersion() { return version; }
        @Override public List<? extends ModVersion> getDependencies() { return List.of(); }
        @Override public List<? extends net.minecraftforge.forgespi.locating.ForgeFeature.Bound> getForgeFeatures() { return List.of(); }
        @Override public String getNamespace() { return data.modId(); }
        @Override public Map<String, Object> getModProperties() { return Map.of(); }
        @Override public Optional<URL> getUpdateURL() { return Optional.empty(); }
        @Override public Optional<URL> getModURL() { return Optional.empty(); }
        @Override public Optional<String> getLogoFile() {
            return data.logoFile() != null ? Optional.of(data.logoFile()) : Optional.empty();
        }
        @Override public boolean getLogoBlur() { return true; }
        @Override public IConfigurable getConfig() { return this; }

        @Override
        public <T> Optional<T> getConfigElement(String... key) { return Optional.empty(); }

        @Override
        public List<? extends IConfigurable> getConfigList(String... key) { return List.of(); }
    }

    // ═══════════════════════════════════════════════════════════
    //  IModFileInfo implementation
    // ═══════════════════════════════════════════════════════════

    static class NeoModFileInfo implements IModFileInfo, IConfigurable {

        private final NeoModData data;
        private List<IModInfo> mods = List.of();

        NeoModFileInfo(NeoModData data) {
            this.data = data;
        }

        void setModInfo(IModInfo modInfo) {
            this.mods = List.of(modInfo);
        }

        @Override public List<IModInfo> getMods() { return mods; }
        @Override public List<LanguageSpec> requiredLanguageLoaders() { return List.of(); }
        @Override public boolean showAsResourcePack() { return false; }
        @Override public Map<String, Object> getFileProperties() { return Map.of(); }
        @Override public String getLicense() { return data.license() != null ? data.license() : "Unknown"; }
        @Override public String moduleName() { return "reforged.neo." + data.modId(); }
        @Override public String versionString() { return data.version(); }
        @Override public List<String> usesServices() { return List.of(); }
        @Override public IModFile getFile() { return null; }
        @Override public IConfigurable getConfig() { return this; }

        @Override
        public <T> Optional<T> getConfigElement(String... key) { return Optional.empty(); }

        @Override
        public List<? extends IConfigurable> getConfigList(String... key) { return List.of(); }
    }

    // ═══════════════════════════════════════════════════════════
    //  ModContainer subclass
    // ═══════════════════════════════════════════════════════════

    static class NeoModContainer extends ModContainer {

        private Object modInstance;

        NeoModContainer(IModInfo info, Object modInstance) {
            super(info);
            this.modInstance = modInstance;
            // Forge's ModLoadingContext.setActiveContainer() calls contextExtension.get()
            // which NPEs if null. Set a dummy supplier.
            this.contextExtension = () -> null;
        }

        /**
         * Update the mod instance after construction (container is created before mod).
         */
        void setModInstance(Object instance) {
            this.modInstance = instance;
        }

        @Override
        public boolean matches(Object mod) {
            return mod == modInstance;
        }

        @Override
        public Object getMod() {
            return modInstance;
        }
    }
}
