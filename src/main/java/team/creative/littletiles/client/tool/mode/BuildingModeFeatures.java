package team.creative.littletiles.client.tool.mode;

import com.mojang.blaze3d.platform.InputConstants;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.settings.KeyModifier;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;

@OnlyIn(Dist.CLIENT)
public class BuildingModeFeatures {
    
    public static final NamedHandlerRegistry<BuildingModeFeature> REGISTRY = new NamedHandlerRegistry<>(null);
    
    public static final BuildingModeToggle TOGGLE_PROPORTIONAL_SCALING = REGISTRY.register("proportional",
        new BuildingModeToggle("building.toggle.proportional", InputConstants.KEY_G, KeyModifier.NONE, false));
    public static final BuildingModeZoom ZOOM = REGISTRY.register("zoom", new BuildingModeZoom());
    public static final BuildingModeGridScroll GRID = REGISTRY.register("grid", new BuildingModeGridScroll());
    
    public static final BuildingModeTopBar TOP_BAR = REGISTRY.register("top_bar", new BuildingModeTopBar());
    
    public static final BuildingModeRules RULES = REGISTRY.register("rules", new BuildingModeRules());
    public static final BuildingModeAreaRule INCLUDE = REGISTRY.register("include", new BuildingModeAreaRule(false));
    public static final BuildingModeAreaRule EXCLUDE = REGISTRY.register("exclude", new BuildingModeAreaRule(true));
    public static final BuildingModeMirrors MIRRORS = REGISTRY.register("mirrors", new BuildingModeMirrors());
}
