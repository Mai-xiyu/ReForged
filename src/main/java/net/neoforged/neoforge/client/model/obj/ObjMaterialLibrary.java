package net.neoforged.neoforge.client.model.obj;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an OBJ material library (.mtl file).
 */
public class ObjMaterialLibrary {
    private final Map<String, Material> materials = new HashMap<>();

    public Map<String, Material> getMaterials() {
        return materials;
    }

    public static class Material {
        public String name;
        public float[] ambientColor = {1, 1, 1};
        public float[] diffuseColor = {1, 1, 1};
        public float[] specularColor = {0, 0, 0};
        public float dissolve = 1.0f;
        public String diffuseColorMap;
    }
}
