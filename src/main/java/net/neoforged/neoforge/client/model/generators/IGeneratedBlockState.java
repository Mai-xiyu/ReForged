package net.neoforged.neoforge.client.model.generators;

import com.google.gson.JsonObject;

/**
 * Marker interface for generated block states that can serialize to JSON.
 */
public interface IGeneratedBlockState {
    JsonObject toJson();
}
