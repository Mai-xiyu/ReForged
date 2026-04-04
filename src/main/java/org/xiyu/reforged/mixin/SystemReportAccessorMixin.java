package org.xiyu.reforged.mixin;

import net.minecraft.SystemReport;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(SystemReport.class)
public abstract class SystemReportAccessorMixin {
    @Shadow @Final private Map<String, String> entries;

    public Map<String, String> getEntries() {
        return this.entries;
    }
}
