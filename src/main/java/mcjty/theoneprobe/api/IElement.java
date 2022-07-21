package mcjty.theoneprobe.api;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * An element in the probe gui.
 */
public interface IElement {

    /**
     * Render this element at the location given by the location
     */
    void render(PoseStack matrixStack, int x, int y);

    /**
     * Get the width of this element
     */
    int getWidth();

    /**
     * Get the height of this element
     */
    int getHeight();

    /**
     * Persist this element to the given network buffer. This should be symmetrical to
     * what IElementFactory.createElement() expects.
     */
    void toBytes(FriendlyByteBuf buf);

    /**
     * Get the identifier for this element (as specified by IElementFactory)
     */
    ResourceLocation getID();
}
