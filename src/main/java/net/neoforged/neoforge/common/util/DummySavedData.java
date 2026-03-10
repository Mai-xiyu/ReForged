package net.neoforged.neoforge.common.util;

import java.io.File;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * A no-op {@link SavedData} implementation which does not store data.
 *
 * @deprecated This type often causes more data corruption than is worth.
 */
@Deprecated(since = "1.21.1", forRemoval = true)
public class DummySavedData extends SavedData {
    public static final DummySavedData DUMMY = new DummySavedData();
    /** @deprecated Use {@link #DUMMY} */
    @Deprecated
    public static final DummySavedData INSTANCE = DUMMY;

    private DummySavedData() {
        super();
    }

    @Override
    public CompoundTag save(final CompoundTag compound, HolderLookup.Provider provider) {
        return null;
    }

    @Override
    public void save(final File file, final HolderLookup.Provider provider) {
        // Do nothing, to avoid overwriting saved data that may already be here.
    }
}
