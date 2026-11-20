package team.creative.littletiles.client.render.material;

import java.io.Reader;
import java.io.StringReader;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;

/** Complete connected end geometry once, before Fusion bakes the model and its children. */
public final class MarbleEndModel {
    private MarbleEndModel() {}

    public static Reader complete(ResourceLocation file, Reader input) {
        if (!file.getNamespace().equals("yuushya")) return input;
        String missing = switch (file.getPath()) {
            case "models/block/chiseled_vertical_marble_top.json",
                 "models/block/template_vertical_block_top.json" -> "down";
            case "models/block/chiseled_vertical_marble_bottom.json",
                 "models/block/template_vertical_block_bottom.json" -> "up";
            case "models/block/template_horizontal_block_left.json" -> "east";
            case "models/block/template_horizontal_block_right.json" -> "west";
            default -> null;
        };
        if (missing == null) return input;
        JsonElement model = JsonParser.parseReader(input);
        if (model.isJsonObject()) completeCube(model.getAsJsonObject(), missing);
        return new StringReader(model.toString());
    }

    private static void completeCube(JsonObject model, String missing) {
        if (!(model.get("elements") instanceof JsonArray elements) || elements.size() != 1
                || !(elements.get(0) instanceof JsonObject cube) || cube.has("rotation")
                || !corner(cube.get("from"), 0) || !corner(cube.get("to"), 16)
                || !(cube.get("faces") instanceof JsonObject faces) || faces.has(missing)) return;
        for (String side : SIDES)
            if (!side.equals(missing) && !(faces.get(side) instanceof JsonObject)) return;
        String present = switch (missing) {
            case "up" -> "down";
            case "down" -> "up";
            case "east" -> "west";
            case "west" -> "east";
            default -> throw new IllegalArgumentException(missing);
        };
        if (!(faces.get(present) instanceof JsonObject original)) return;
        JsonElement cull = original.get("cullface");
        if (cull != null && (!cull.isJsonPrimitive() || !present.equals(cull.getAsString()))) return;
        JsonObject cap = original.deepCopy();
        if (cull != null) cap.addProperty("cullface", missing);
        faces.add(missing, cap);
    }

    private static final String[] SIDES = {"up", "down", "north", "south", "east", "west"};

    private static boolean corner(JsonElement value, int coordinate) {
        if (!(value instanceof JsonArray axes) || axes.size() != 3) return false;
        for (JsonElement axis : axes)
            if (!axis.isJsonPrimitive() || !axis.getAsJsonPrimitive().isNumber() || axis.getAsDouble() != coordinate)
                return false;
        return true;
    }
}
