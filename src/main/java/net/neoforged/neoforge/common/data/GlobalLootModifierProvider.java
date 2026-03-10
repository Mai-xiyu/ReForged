package net.neoforged.neoforge.common.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Provider for NeoForge's GlobalLootModifier system. See {@link LootModifier}.
 * <p>
 * This provider only requires implementing {@link #start()} and calling {@link #add} from it.
 */
public abstract class GlobalLootModifierProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registriesLookup;
    protected HolderLookup.Provider registries;
    private final String modid;
    private final Map<String, IGlobalLootModifier> toSerialize = new LinkedHashMap<>();
    private boolean replace = false;

    public GlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modid) {
        this.output = output;
        this.registriesLookup = registries;
        this.modid = modid;
    }

    /** Sets the "replace" key in global_loot_modifiers to true. */
    protected void replacing() {
        this.replace = true;
    }

    /** Call {@link #add} here, which will pass in the necessary information to write the jsons. */
    protected abstract void start();

    @Override
    public final CompletableFuture<?> run(CachedOutput cache) {
        return this.registriesLookup.thenCompose(registries -> this.run(cache, registries));
    }

    protected CompletableFuture<?> run(CachedOutput cache, HolderLookup.Provider registries) {
        this.registries = registries;
        start();

        Path forgePath = this.output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve("neoforge").resolve("loot_modifiers").resolve("global_loot_modifiers.json");
        Path modifierFolderPath = this.output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(this.modid).resolve("loot_modifiers");
        List<ResourceLocation> entries = new ArrayList<>();
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (var entry : toSerialize.entrySet()) {
            entries.add(ResourceLocation.fromNamespaceAndPath(modid, entry.getKey()));
        }

        JsonObject forgeJson = new JsonObject();
        forgeJson.addProperty("replace", this.replace);
        forgeJson.add("entries", GSON.toJsonTree(
                entries.stream().map(ResourceLocation::toString).collect(Collectors.toList())));

        futures.add(DataProvider.saveStable(cache, forgeJson, forgePath));
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    public <T extends IGlobalLootModifier> void add(String modifier, T instance, List<ICondition> conditions) {
        this.toSerialize.put(modifier, instance);
    }

    public <T extends IGlobalLootModifier> void add(String modifier, T instance, ICondition... conditions) {
        add(modifier, instance, Arrays.asList(conditions));
    }

    @Override
    public String getName() {
        return "Global Loot Modifiers : " + modid;
    }
}
