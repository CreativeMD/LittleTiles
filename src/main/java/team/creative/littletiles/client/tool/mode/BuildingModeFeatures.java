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
}
