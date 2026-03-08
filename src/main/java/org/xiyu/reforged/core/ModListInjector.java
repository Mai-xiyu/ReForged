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
import java.lang.reflect.Proxy;
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
                              Object modInstance,
                              net.minecraftforge.forgespi.language.ModFileScanData scanData,
                              java.nio.file.Path jarPath) {}

    /** Backwards-compatible factory without scan data. */
    public static NeoModData createNeoModData(String modId, String displayName, String version,
                                               String description, String license, String logoFile,
                                               Object modInstance) {
        return new NeoModData(modId, displayName, version, description, license, logoFile,
                modInstance, new net.minecraftforge.forgespi.language.ModFileScanData(), null);
    }

    /**
     * Create a {@link NeoModContainer} (Forge ModContainer subclass) from a {@link NeoModData}.
     * The container is created with a null mod instance; call {@link NeoModContainer#setModInstance(Object)}
     * after the mod is constructed.
     *
     * <p>This is used before mod construction so that Forge's {@code ModLoadingContext.setActiveContainer()}
     * can be called with the correct container, ensuring configs register under the correct mod ID.</p>
     */
    public static NeoModContainer createContainer(NeoModData data) {
        NeoModFile modFile = new NeoModFile(data);
        NeoModFileInfo fileInfo = new NeoModFileInfo(data, modFile);
        modFile.setModFileInfo(fileInfo);
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
            Field modFilesField = ModList.class.getDeclaredField("modFiles");
            Field fileByIdField = ModList.class.getDeclaredField("fileById");

            modsField.setAccessible(true);
            indexedModsField.setAccessible(true);
            sortedContainersField.setAccessible(true);
            sortedListField.setAccessible(true);
            scanDataField.setAccessible(true);
            modFilesField.setAccessible(true);
            fileByIdField.setAccessible(true);

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
                @SuppressWarnings("unchecked")
                List<IModFileInfo> existingModFiles =
                    (List<IModFileInfo>) modFilesField.get(modList);
                @SuppressWarnings("unchecked")
                Map<String, IModFileInfo> existingFileById =
                    (Map<String, IModFileInfo>) (Map<?, ?>) fileByIdField.get(modList);

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

            boolean modFilesMutated = false;
            boolean fileByIdMutated = false;

            for (NeoModContainer container : containers) {
                IModInfo modInfo = container.getModInfo();

                // Skip if already injected (e.g. early injection before construction)
                if (newIndexed.containsKey(container.getModId())) continue;

                newMods.add(container);
                newIndexed.put(container.getModId(), container);
                newSorted.add(container);
                newSortedList.add(modInfo);

                // Register in NeoForge ModList for getModFileById() fallback
                IModFileInfo forgeFileInfo = container.getModInfo().getOwningFile();
                if (forgeFileInfo != null) {
                    // Only add to modFiles if it's a real Forge ModFileInfo.
                    // NeoModFileInfo can't be cast to ModFileInfo in crash report code
                    // (ModList.fileToLine casts IModFileInfo to concrete ModFileInfo).
                    if (!existingModFiles.contains(forgeFileInfo)
                            && forgeFileInfo.getClass().getName().contains("ModFileInfo")
                            && !(forgeFileInfo instanceof NeoModFileInfo)) {
                        existingModFiles.add(forgeFileInfo);
                        modFilesMutated = true;
                    }
                    existingFileById.put(container.getModId(), forgeFileInfo);
                    fileByIdMutated = true;

                    net.neoforged.neoforgespi.language.IModFileInfo neoFileInfo =
                            net.neoforged.neoforgespi.language.IModFileInfo.wrap(forgeFileInfo);
                    net.neoforged.fml.ModList.registerNeoModFileInfo(container.getModId(), neoFileInfo);
                }

                LOGGER.info("[ReForged] Registered '{}' in Forge mod list", container.getModId());
            }

            // Replace fields atomically
            modsField.set(modList, Collections.unmodifiableList(newMods));
            indexedModsField.set(modList, Collections.unmodifiableMap(newIndexed));
            sortedContainersField.set(modList, Collections.unmodifiableList(newSorted));
            sortedListField.set(modList, Collections.unmodifiableList(newSortedList));
            List<net.minecraftforge.forgespi.language.ModFileScanData> newScanData = newSortedList.stream()
                    .map(IModInfo::getOwningFile)
                    .filter(Objects::nonNull)
                    .map(IModFileInfo::getFile)
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(IModFile::getScanResult)
                    .filter(Objects::nonNull)
                    .toList();
            scanDataField.set(modList, newScanData);

            LOGGER.info("[ReForged] ModList injection complete: +{} mods, modFilesMutated={}, fileByIdMutated={}",
                    containers.size(), modFilesMutated, fileByIdMutated);

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

    // ═══════════════════════════════════════════════════════════
    //  IModFile implementation (provides scan results to NeoForge mods)
    // ═══════════════════════════════════════════════════════════

    static class NeoModFile implements IModFile {
        private final NeoModData data;
        private NeoModFileInfo modFileInfo;

        NeoModFile(NeoModData data) {
            this.data = data;
        }
                private static final net.minecraftforge.forgespi.locating.IModProvider FALLBACK_PROVIDER =
                        (net.minecraftforge.forgespi.locating.IModProvider) Proxy.newProxyInstance(
                                net.minecraftforge.forgespi.locating.IModProvider.class.getClassLoader(),
                                new Class<?>[]{net.minecraftforge.forgespi.locating.IModProvider.class},
                                (proxy, method, args) -> {
                                    if (method.getDeclaringClass() == Object.class) {
                                        return switch (method.getName()) {
                                            case "toString" -> "ReForgedNeoBridgeProvider";
                                            case "hashCode" -> System.identityHashCode(proxy);
                                            case "equals" -> proxy == args[0];
                                            default -> null;
                                        };
                                    }
                                    if ("name".equals(method.getName())) {
                                        return "ReForgedNeoBridge";
                                    }
                                    Class<?> returnType = method.getReturnType();
                                    if (returnType == boolean.class) return false;
                                    if (returnType == byte.class) return (byte) 0;
                                    if (returnType == short.class) return (short) 0;
                                    if (returnType == int.class) return 0;
                                    if (returnType == long.class) return 0L;
                                    if (returnType == float.class) return 0f;
                                    if (returnType == double.class) return 0d;
                                    if (returnType == char.class) return '\0';
                                    return null;
                                }
                        );


        void setModFileInfo(NeoModFileInfo info) {
            this.modFileInfo = info;
        }

        @Override
        public List<net.minecraftforge.forgespi.language.IModLanguageProvider> getLoaders() {
            return List.of();
        }

        @Override
        public java.nio.file.Path findResource(String... pathName) {
            if (data.jarPath() == null) return null;
            try {
                java.nio.file.FileSystem fs = java.nio.file.FileSystems.newFileSystem(data.jarPath(), (ClassLoader) null);
                return fs.getPath(String.join("/", pathName));
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public java.util.function.Supplier<Map<String, Object>> getSubstitutionMap() {
            return Map::of;
        }

        @Override
        public Type getType() {
            return Type.MOD;
        }

        @Override
        public java.nio.file.Path getFilePath() {
            return data.jarPath();
        }

        @Override
        public cpw.mods.jarhandling.SecureJar getSecureJar() {
            return null;
        }

        @Override
        public void setSecurityStatus(cpw.mods.jarhandling.SecureJar.Status status) {
        }

        @Override
        public List<net.minecraftforge.forgespi.language.IModInfo> getModInfos() {
            return modFileInfo != null ? modFileInfo.getMods() : List.of();
        }

        @Override
        public net.minecraftforge.forgespi.language.ModFileScanData getScanResult() {
            return data.scanData() != null ? data.scanData()
                    : new net.minecraftforge.forgespi.language.ModFileScanData();
        }

        @Override
        public String getFileName() {
            return data.jarPath() != null ? data.jarPath().getFileName().toString() : "unknown";
        }

        @Override
        public net.minecraftforge.forgespi.locating.IModProvider getProvider() {
            return FALLBACK_PROVIDER;
        }

        @Override
        public IModFileInfo getModFileInfo() {
            return modFileInfo;
        }
    }

    static class NeoModFileInfo implements IModFileInfo, IConfigurable {

        private final NeoModData data;
        private final NeoModFile modFile;
        private List<IModInfo> mods = List.of();

        NeoModFileInfo(NeoModData data, NeoModFile modFile) {
            this.data = data;
            this.modFile = modFile;
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
        @Override public IModFile getFile() { return modFile; }
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
