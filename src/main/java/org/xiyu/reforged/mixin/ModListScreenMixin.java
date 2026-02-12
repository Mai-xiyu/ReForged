package org.xiyu.reforged.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.client.gui.widget.ModListWidget;
import net.minecraftforge.common.util.MavenVersionStringHelper;
import net.minecraftforge.common.util.Size2i;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.common.ForgeI18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Fixes a {@link ClassCastException} in {@code ModListScreen.updateCache()} where
 * Forge hardcodes a cast from {@code IModFileInfo} to the concrete {@code ModFileInfo}.
 *
 * <p>Our NeoForge mod entries use a custom {@code IModFileInfo} implementation.
 * This mixin intercepts {@code updateCache()} at HEAD: if the selected mod's
 * owning file is NOT a {@code ModFileInfo}, we build the info panel ourselves
 * and cancel the original method entirely.</p>
 */
@Mixin(value = ModListScreen.class, remap = false)
public class ModListScreenMixin {

    @Shadow private ModListWidget.ModEntry selected;
    @Shadow private Button configButton;

    @Inject(method = "updateCache", at = @At("HEAD"), cancellable = true)
    private void reforged$handleNeoModDisplay(CallbackInfo ci) {
        if (selected == null) return; // let original handle null selection

        IModInfo info = selected.getInfo();
        IModFileInfo owningFile = info.getOwningFile();

        // Standard Forge mods → let the original method run untouched
        if (owningFile instanceof ModFileInfo) return;

        // ── NeoForge mod detected — build display info safely ──
        configButton.active = false;

        List<String> lines = new ArrayList<>();
        lines.add(info.getDisplayName());
        lines.add(ForgeI18n.parseMessage("fml.menu.mods.info.version",
                MavenVersionStringHelper.artifactVersionToString(info.getVersion())));
        lines.add(ForgeI18n.parseMessage("fml.menu.mods.info.idstate",
                info.getModId(), "LOADED"));

        info.getConfig().getConfigElement("credits").ifPresent(credits ->
                lines.add(ForgeI18n.parseMessage("fml.menu.mods.info.credits", credits)));
        info.getConfig().getConfigElement("authors").ifPresent(authors ->
                lines.add(ForgeI18n.parseMessage("fml.menu.mods.info.authors", authors)));
        info.getConfig().getConfigElement("displayURL").ifPresent(displayURL ->
                lines.add(ForgeI18n.parseMessage("fml.menu.mods.info.displayurl", displayURL)));

        lines.add(ForgeI18n.parseMessage("fml.menu.mods.info.nochildmods"));

        String license = owningFile != null ? owningFile.getLicense() : "Unknown";
        lines.add(ForgeI18n.parseMessage("fml.menu.mods.info.license", license));

        lines.add(null);
        lines.add(info.getDescription());

        // Access the private InfoPanel field via reflection (it's a private inner class)
        try {
            Field modInfoField = ModListScreen.class.getDeclaredField("modInfo");
            modInfoField.setAccessible(true);
            Object panel = modInfoField.get(this);
            Method setInfo = panel.getClass().getDeclaredMethod(
                    "setInfo", List.class, ResourceLocation.class, Size2i.class);
            setInfo.setAccessible(true);
            setInfo.invoke(panel, lines, null, new Size2i(0, 0));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ci.cancel();
    }
}
