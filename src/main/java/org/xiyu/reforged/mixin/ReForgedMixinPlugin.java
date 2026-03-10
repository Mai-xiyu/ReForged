package org.xiyu.reforged.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Conditionally applies Mixins that depend on optional NeoForge mods.
 * <p>
 * FontJadePatchMixin requires the Jade mod to be present (its JadeFont interface
 * must be on the classpath). If Jade is not installed, the Mixin is skipped.
 * <p>
 * After applying FontJadePatchMixin, the plugin adds {@code snownee.jade.gui.JadeFont}
 * interface to Font's class node via ASM, avoiding compile-time dependency on Jade.
 */
public class ReForgedMixinPlugin implements IMixinConfigPlugin {

    private boolean jadePresent;
    private boolean balmPresent;

    @Override
    public void onLoad(String mixinPackage) {
        try {
            Class.forName("snownee.jade.gui.JadeFont", false,
                    getClass().getClassLoader());
            jadePresent = true;
        } catch (ClassNotFoundException e) {
            jadePresent = false;
        }
        try {
            Class.forName("net.blay09.mods.balm.api.entity.BalmEntity", false,
                    getClass().getClassLoader());
            balmPresent = true;
        } catch (ClassNotFoundException e) {
            balmPresent = false;
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("FontJadePatchMixin")) {
            return jadePresent;
        }
        // Skip our BalmEntityMixin when Balm is present — Balm's own mixin already adds these methods
        if (mixinClassName.endsWith("BalmEntityMixin")) {
            return !balmPresent;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass,
                         String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass,
                          String mixinClassName, IMixinInfo mixinInfo) {
        // After FontJadePatchMixin adds the method implementations,
        // add the JadeFont interface to Font's class node via ASM
        if (mixinClassName.endsWith("FontJadePatchMixin") && jadePresent) {
            String jadeFontInternal = "snownee/jade/gui/JadeFont";
            if (!targetClass.interfaces.contains(jadeFontInternal)) {
                targetClass.interfaces.add(jadeFontInternal);
            }
        }
    }
}
