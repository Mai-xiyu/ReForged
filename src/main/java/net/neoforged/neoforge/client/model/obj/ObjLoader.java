package net.neoforged.neoforge.client.model.obj;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

/**
 * Geometry loader for OBJ model files.
 * Delegates to Forge's OBJ loader implementation.
 */
public class ObjLoader implements IGeometryLoader<ObjModel> {
    public static final ObjLoader INSTANCE = new ObjLoader();

    private ObjLoader() {}

    @Override
    public ObjModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
        // Delegate to Forge's ObjLoader and wrap the result
        net.minecraftforge.client.model.obj.ObjModel forgeModel =
                net.minecraftforge.client.model.obj.ObjLoader.INSTANCE.read(jsonObject, deserializationContext);
        return new ObjModel(forgeModel);
    }
}
