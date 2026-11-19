package team.creative.littletiles.client.render.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.BlockState;

/** Resolves cut faces for connected variants that share a rotated end model. */
public final class RotatedCutFaces {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int ALL_FACES = (1 << DIRECTIONS.length) - 1;
    private static final Map<ModelBakery, Map<BlockState, StateDonor>> PREPARED = new WeakHashMap<>();
    private static volatile Map<BlockState, StateDonor> active = Map.of();

    private RotatedCutFaces() {}

    public record Donor(String variant, int missingFaces) {}
    public record StateDonor(BlockState state, int missingFaces) {}

    public static BlockState donor(BlockState state, Direction face) {
        var donor = active.get(state);
        return donor != null && (donor.missingFaces() & bit(face)) != 0 ? donor.state() : null;
    }

    public static synchronized void prepare(ModelBakery bakery, Map<BlockState, StateDonor> donors) {
        PREPARED.put(bakery, donors);
    }

    public static synchronized void apply(ModelBakery bakery) {
        var donors = PREPARED.remove(bakery);
        active = donors == null ? Map.of() : donors;
    }

    public static Map<BlockState, StateDonor> resolve(Map<ResourceLocation, Map<String, Donor>> candidates) {
        var result = new HashMap<BlockState, StateDonor>();
        for (var entry : candidates.entrySet()) {
            var block = BuiltInRegistries.BLOCK.getOptional(entry.getKey()).orElse(null);
            if (block == null) continue;
            var byVariant = new HashMap<String, BlockState>();
            for (var state : block.getStateDefinition().getPossibleStates()) {
                byVariant.put(BlockModelShaper.statePropertiesToString(state.getValues()), state);
            }
            for (var candidate : entry.getValue().entrySet()) {
                var source = byVariant.get(candidate.getKey());
                var donor = byVariant.get(candidate.getValue().variant());
                if (source != null && donor != null && source != donor) {
                    result.put(source, new StateDonor(donor, candidate.getValue().missingFaces()));
                }
            }
        }
        // A fallback target must terminate the lookup, even if a block has several connection properties.
        var sources = Set.copyOf(result.keySet());
        result.values().removeIf(donor -> sources.contains(donor.state()));
        return Map.copyOf(result);
    }

    public static Map<ResourceLocation, Map<String, Donor>> find(Map<ResourceLocation, BlockModel> models,
            Map<ResourceLocation, List<BlockStateModelLoader.LoadedJson>> states) {
        var result = new HashMap<ResourceLocation, Map<String, Donor>>();
        var donors = new HashMap<String, Donor>();
        var conflicts = new HashSet<String>();
        for (var entry : states.entrySet()) {
            var stack = entry.getValue();
            if (stack.size() != 1 || !(stack.getFirst().data() instanceof JsonObject state)
                    || state.has("multipart") || !(state.get("variants") instanceof JsonObject variants)) continue;
            for (var variant : variants.entrySet()) {
                String key = variant.getKey();
                if (!key.contains("=none") && !key.contains("=single")) continue;
                String[] parts = key.split(",");
                for (int slot = 0; slot < parts.length; slot++) {
                    String term = parts[slot];
                    int equals = term.indexOf('=');
                    if (equals < 1 || !(term.endsWith("=none") || term.endsWith("=single"))) continue;
                    String prefix = term.substring(0, equals + 1);
                    checkPair(models, variants, parts, slot, prefix, key, "top", "bottom", donors, conflicts);
                    checkPair(models, variants, parts, slot, prefix, key, "left", "right", donors, conflicts);
                    parts[slot] = term;
                }
            }
            conflicts.forEach(donors::remove);
            if (!donors.isEmpty()) {
                result.put(BlockStateModelLoader.BLOCKSTATE_LISTER.fileToId(entry.getKey()), Map.copyOf(donors));
            }
            donors.clear();
            conflicts.clear();
        }
        return Map.copyOf(result);
    }

    private static void checkPair(Map<ResourceLocation, BlockModel> models, JsonObject variants, String[] parts, int slot,
            String prefix, String key, String first, String last, Map<String, Donor> donors, Set<String> conflicts) {
        parts[slot] = prefix + first;
        String firstKey = String.join(",", parts);
        parts[slot] = prefix + last;
        String lastKey = String.join(",", parts);
        if (!(variants.get(firstKey) instanceof JsonObject firstVariant)
                || !(variants.get(lastKey) instanceof JsonObject lastVariant)
                || !java.util.Objects.equals(firstVariant.get("model"), lastVariant.get("model"))) return;
        var a = choice(firstVariant);
        var b = choice(lastVariant);
        if (a == null || b == null || a.rotation() == b.rotation()) return;
        parts[slot] = prefix + "middle";
        String middleKey = String.join(",", parts);
        var middle = choice(variants.get(middleKey));
        var single = choice(variants.get(key));
        if (single == null || middle == null || single.model().equals(a.model()) || middle.model().equals(single.model())) return;
        var endModel = ConnectedEndModels.lookup(models, a.model());
        var singleModel = ConnectedEndModels.lookup(models, single.model());
        var end = ConnectedEndModels.cube(models, endModel, true);
        var closed = ConnectedEndModels.cube(models, singleModel, true);
        var center = ConnectedEndModels.cube(models, ConnectedEndModels.lookup(models, middle.model()), true);
        if (end == null || closed == null || center == null || end.faces.size() != 5 || closed.faces.size() != 6
                || center.faces.size() != 4 || center.shade != closed.shade) return;
        int missingA = missing(end, a);
        int missingB = missing(end, b);
        Direction faceA = DIRECTIONS[Integer.numberOfTrailingZeros(missingA)];
        Direction faceB = faceA.getOpposite();
        if (missingB != bit(faceB) || missing(center, middle) != (missingA | missingB)) return;
        Direction localMissing = localDirection(a, faceA);
        if (!matches(models, endModel, end, localMissing, singleModel, closed, localDirection(single, faceA))
                || !matches(models, endModel, end, localMissing, singleModel, closed, localDirection(single, faceB))) return;
        String donor = canonical(key);
        add(donors, conflicts, firstKey, new Donor(donor, missingA));
        add(donors, conflicts, lastKey, new Donor(donor, missingB));
        add(donors, conflicts, middleKey, new Donor(donor, missingA | missingB));
    }

    private static Choice choice(JsonElement element) {
        if (!(element instanceof JsonObject json)) return null;
        try {
            var model = ResourceLocation.tryParse(GsonHelper.getAsString(json, "model"));
            var rotation = BlockModelRotation.by(GsonHelper.getAsInt(json, "x", 0), GsonHelper.getAsInt(json, "y", 0));
            return model == null || rotation == null ? null : new Choice(model, rotation);
        } catch (com.google.gson.JsonParseException | NumberFormatException ignored) {
            return null;
        }
    }

    private static int missing(BlockElement element, Choice choice) {
        int present = 0;
        for (var side : element.faces.keySet()) present |= bit(choice.rotation().actualRotation().rotate(side));
        return ALL_FACES ^ present;
    }

    private static Direction localDirection(Choice choice, Direction world) {
        return choice.rotation().actualRotation().inverse().rotate(world);
    }

    private static boolean matches(Map<ResourceLocation, BlockModel> models, BlockModel endModel, BlockElement end,
            Direction localMissing, BlockModel singleModel, BlockElement closed, Direction donorSide) {
        Direction opposite = localMissing.getOpposite();
        var outside = end.faces.get(opposite);
        var donor = closed.faces.get(donorSide);
        var material = ConnectedEndModels.texture(models, endModel, outside.texture());
        return material != null && material.equals(ConnectedEndModels.texture(models, singleModel, donor.texture()))
                && end.shade == closed.shade && outside.tintIndex() == donor.tintIndex()
                && outside.faceData().equals(donor.faceData())
                && (outside.cullForDirection() == null || outside.cullForDirection() == opposite)
                && (donor.cullForDirection() == null || donor.cullForDirection() == donorSide);
    }

    private static void add(Map<String, Donor> donors, Set<String> conflicts, String key, Donor donor) {
        String variant = canonical(key);
        var previous = donors.putIfAbsent(variant, donor);
        if (previous != null && !previous.equals(donor)) conflicts.add(variant);
    }

    private static String canonical(String variant) {
        String[] parts = variant.split(",");
        Arrays.sort(parts);
        return String.join(",", parts);
    }

    private static int bit(Direction direction) {
        return 1 << direction.ordinal();
    }

    private record Choice(ResourceLocation model, BlockModelRotation rotation) {}
}
