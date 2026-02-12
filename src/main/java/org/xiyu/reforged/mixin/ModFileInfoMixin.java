package org.xiyu.reforged.mixin;

import com.mojang.logging.LogUtils;
import net.minecraftforge.forgespi.locating.IModFile;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;
import java.nio.file.*;

/**
 * Mixin into Forge's ModFileParser to also accept {@code neoforge.mods.toml}.
 *
 * <h3>Problem</h3>
 * <p>Forge's {@code ModFileParser.readModList()} only looks for {@code META-INF/mods.toml}.
 * NeoForge mods have {@code META-INF/neoforge.mods.toml} instead.</p>
 *
 * <h3>Solution</h3>
 * <p>Intercept the mod file parsing. When a mod file doesn't have {@code mods.toml}
 * but does have {@code neoforge.mods.toml}, convert the NeoForge descriptor on-the-fly
 * and inject it as if it were a standard {@code mods.toml}.</p>
 *
 * <p>Target class: {@code net.minecraftforge.fml.loading.moddiscovery.ModFileParser}</p>
 */
@Mixin(targets = "net.minecraftforge.fml.loading.moddiscovery.ModFileParser", remap = false)
public abstract class ModFileInfoMixin {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Inject at the HEAD of readModList to intercept mod file loading.
     * If the mod file contains neoforge.mods.toml but no mods.toml,
     * convert and inject the descriptor.
     */
    @Inject(method = "readModList", at = @At("HEAD"), cancellable = false, remap = false)
    private static void reforged$onReadModList(IModFile modFile, CallbackInfoReturnable<?> cir) {
        try {
            Path modPath = modFile.getFilePath();
            LOGGER.debug("[ReForged] ModFileInfoMixin: Checking {}", modPath.getFileName());

            // Check if this mod file has neoforge.mods.toml
            Path neoToml = modPath.resolve("META-INF").resolve("neoforge.mods.toml");
            Path forgeToml = modPath.resolve("META-INF").resolve("mods.toml");

            if (Files.exists(neoToml) && !Files.exists(forgeToml)) {
                LOGGER.info("[ReForged] Detected NeoForge mod: {} — converting neoforge.mods.toml → mods.toml",
                        modPath.getFileName());

                // Read and convert the NeoForge descriptor
                String neoContent = Files.readString(neoToml);
                String forgeContent = org.xiyu.reforged.core.ModDescriptorConverter.convert(neoContent);

                // Write the converted mods.toml into the mod file
                Files.createDirectories(forgeToml.getParent());
                Files.writeString(forgeToml, forgeContent);

                LOGGER.info("[ReForged] Successfully injected mods.toml for NeoForge mod: {}", modPath.getFileName());
            }
        } catch (Exception e) {
            LOGGER.error("[ReForged] ModFileInfoMixin: Failed to process mod file", e);
        }
    }
}
