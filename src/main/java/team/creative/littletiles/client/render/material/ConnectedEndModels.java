package team.creative.littletiles.client.render.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.resources.model.BlockStateModelLoader.LoadedJson;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** Validate symmetric end variants while all reload inputs are available, before parents are linked. */
public final class ConnectedEndModels {
    private static final Set<String> SUPPORTED_NAMESPACES = Set.of("yuushya", "furniture");

    private ConnectedEndModels() {}

    public static Map<ResourceLocation, List<LoadedJson>> supportedStates(Map<ResourceLocation, List<LoadedJson>> states) {
        var supported = new HashMap<ResourceLocation, List<LoadedJson>>();
        for (var entry : states.entrySet()) {
            if (SUPPORTED_NAMESPACES.contains(entry.getKey().getNamespace())) {
                supported.put(entry.getKey(), entry.getValue());
            }
        }
        return supported;
    }

    public static void complete(Map<ResourceLocation, BlockModel> models, Map<ResourceLocation, List<LoadedJson>> states) {
        for (var stack : states.values()) {
            if (stack.size() != 1 || !(stack.get(0).data() instanceof JsonObject state)
                    || state.has("multipart") || !(state.get("variants") instanceof JsonObject variants)) continue;
            for (var entry : variants.entrySet()) {
                String key = entry.getKey();
                if (!key.contains("=none") && !key.contains("=single")) continue;
                for (String term : key.split(",")) {
                    int equals = term.indexOf('=');
                    if (equals < 1) continue;
                    String value = term.substring(equals + 1);
                    if (!value.equals("none") && !value.equals("single")) continue;
                    String prefix = term.substring(0, equals + 1);
                    String[] parts = key.split(",");
                    int slot = Arrays.asList(parts).indexOf(term);
                    if (!(entry.getValue() instanceof JsonObject reference)) continue;
                    parts[slot] = prefix + "middle";
                    if (variant(models, variants.get(String.join(",", parts)), reference) == null) continue;
                    checkPair(models, variants, parts, slot, prefix, reference, "top", "bottom");
                    checkPair(models, variants, parts, slot, prefix, reference, "left", "right");
                }
            }
        }
    }

    private static void checkPair(Map<ResourceLocation, BlockModel> models, JsonObject variants, String[] parts,
            int slot, String prefix, JsonObject reference, String first, String last) {
        parts[slot] = prefix + first;
        BlockModel a = variant(models, variants.get(String.join(",", parts)), reference);
        if (a == null) return;
        parts[slot] = prefix + last;
        BlockModel b = variant(models, variants.get(String.join(",", parts)), reference);
        BlockModel single = variant(models, reference, reference);
        if (b == null || single == null || a == b || a == single || b == single) return;
        BlockElement one = cube(models, a), two = cube(models, b), closed = cube(models, single);
        if (one == null || two == null || closed == null || one.faces.size() != 5 || two.faces.size() != 5
                || closed.faces.size() != 6) return;
        for (Direction side : Direction.values()) {
            if (one.faces.containsKey(side)) continue;
            Direction opposite = side.getOpposite();
            if (two.faces.containsKey(opposite)) return;
            if (matches(models, a, one, single, closed, side) && matches(models, b, two, single, closed, opposite)) {
                add(a, one, side);
                add(b, two, opposite);
            }
            return;
        }
    }

    private static BlockModel variant(Map<ResourceLocation, BlockModel> models, JsonElement json, JsonObject reference) {
        if (!(json instanceof JsonObject obj) || !obj.has("model") || !obj.get("model").isJsonPrimitive()) return null;
        for (String key : List.of("x", "y", "uvlock"))
            if (!java.util.Objects.equals(obj.get(key), reference.get(key))) return null;
        var id = ResourceLocation.tryParse(obj.get("model").getAsString());
        return id == null ? null : lookup(models, id);
    }

    static BlockModel lookup(Map<ResourceLocation, BlockModel> models, ResourceLocation id) {
        return id == null ? null : models.get(id.withPath(path -> "models/" + path + ".json"));
    }

    private static boolean vanilla(BlockModel model) {
        return model != null && model.getClass() == BlockModel.class && model.parent == null && !model.customData.hasCustomGeometry();
    }

    private static BlockElement cube(Map<ResourceLocation, BlockModel> models, BlockModel model) {
        return cube(models, model, false);
    }

    static BlockElement cube(Map<ResourceLocation, BlockModel> models, BlockModel model, boolean allowIdentityRotation) {
        for (int depth = 0; depth < 32 && vanilla(model); depth++, model = lookup(models, model.getParentLocation())) {
            var elements = model.getElements();
            if (elements.isEmpty()) continue;
            if (elements.size() != 1) return null;
            var e = elements.get(0);
            boolean unrotated = e.rotation == null
                    || (allowIdentityRotation && e.rotation.angle() == 0 && !e.rotation.rescale());
            return unrotated && e.from.equals(0, 0, 0) && e.to.equals(16, 16, 16) ? e : null;
        }
        return null;
    }

    static Material texture(Map<ResourceLocation, BlockModel> models, BlockModel child, String name) {
        String key = name.startsWith("#") ? name.substring(1) : name;
        for (int alias = 0; alias < 32; alias++) {
            BlockModel model = child;
            boolean found = false;
            for (int depth = 0; depth < 32 && vanilla(model); depth++, model = lookup(models, model.getParentLocation())) {
                var binding = model.textureMap.get(key);
                if (binding == null) continue;
                if (binding.left().isPresent()) return binding.left().get();
                key = binding.right().orElseThrow();
                found = true;
                break;
            }
            if (!found) return null;
        }
        return null;
    }

    private static boolean matches(Map<ResourceLocation, BlockModel> models, BlockModel end, BlockElement open,
            BlockModel single, BlockElement closed, Direction missing) {
        var src = open.faces.get(missing.getOpposite());
        var ref = closed.faces.get(missing);
        if (src.cullForDirection() != missing.getOpposite() || ref.cullForDirection() != missing
                || open.shade != closed.shade || src.tintIndex() != ref.tintIndex()
                || !src.faceData().equals(ref.faceData()) || src.uv().rotation != ref.uv().rotation
                || !Arrays.equals(src.uv().uvs, ref.uv().uvs)) return false;
        Material actual = texture(models, end, src.texture());
        return actual != null && actual.equals(texture(models, single, ref.texture()));
    }

    private static void add(BlockModel model, BlockElement cube, Direction side) {
        var src = cube.faces.get(side.getOpposite());
        var cap = new BlockElementFace(side, src.tintIndex(), src.texture(), src.uv(), src.faceData(),
                new org.apache.commons.lang3.mutable.MutableObject<>());
        if (model.getElements().isEmpty()) {
            // Localize inherited geometry so siblings sharing the parent keep their own shape.
            var faces = new EnumMap<Direction, BlockElementFace>(Direction.class);
            cube.faces.forEach((d, f) -> faces.put(d, new BlockElementFace(f.cullForDirection(), f.tintIndex(),
                    f.texture(), f.uv(), f.faceData(), new org.apache.commons.lang3.mutable.MutableObject<>())));
            faces.put(side, cap);
            model.getElements().add(new BlockElement(cube.from, cube.to, faces, null, cube.shade, cube.getFaceData()));
        } else {
            cap.parent().setValue(cube);
            cube.faces.put(side, cap);
        }
    }
}
