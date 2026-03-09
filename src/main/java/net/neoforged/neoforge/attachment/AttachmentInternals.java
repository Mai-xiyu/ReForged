package net.neoforged.neoforge.attachment;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import org.xiyu.reforged.bridge.NeoAttachmentHolderBridge;

public final class AttachmentInternals {
    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        MinecraftForge.EVENT_BUS.addListener(AttachmentInternals::onPlayerClone);
        MinecraftForge.EVENT_BUS.addListener(AttachmentInternals::onLivingConvert);
    }

    public static void copyEntityAttachments(Entity from, Entity to, boolean isDeath) {
        if (!(from instanceof NeoAttachmentHolderBridge fromBridge) || !(to instanceof NeoAttachmentHolderBridge toBridge)) {
            return;
        }

        AttachmentHolder.AsField fromHolder = fromBridge.reforged$getNeoAttachmentHolder();
        AttachmentHolder.AsField toHolder = toBridge.reforged$getNeoAttachmentHolder();
        fromHolder.copyAttachmentsTo(from.registryAccess(), toHolder, isDeath ? AttachmentType::copyOnDeath : type -> true);
    }

    public static void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        copyEntityAttachments(event.getOriginal(), event.getEntity(), event.isWasDeath());
    }

    public static void onLivingConvert(net.minecraftforge.event.entity.living.LivingConversionEvent.Post event) {
        copyEntityAttachments(event.getEntity(), event.getOutcome(), true);
    }

    private AttachmentInternals() {
    }
}