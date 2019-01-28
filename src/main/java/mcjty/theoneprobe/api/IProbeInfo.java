package mcjty.theoneprobe.api;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Information to return to the probe. Most methods here return the same probe info
 * object so that you can chain:
 *     probeInfo.item(diamond).text("My diamond").text("Next line");
 *
 * horizontal() and vertical() are exceptions. They return a new probe info object
 * representing the new horizontal or vertical layout that was created. Keep that in mind!
 *
 * Note that you can safeul use TextFormatting color codes in the text.
 */
public interface IProbeInfo {

    /// Use STARTLOC/ENDLOC in your strings for localization on the client
    public static final String STARTLOC = "{*";
    public static final String ENDLOC = "*}";

    /**
     * Create a default layout style for the horizontal or vertical elements
     */
    ILayoutStyle defaultLayoutStyle();

    /**
     * Create a default style for the progress bar
     */
    IProgressStyle defaultProgressStyle();

    /**
     * Create a default style for the text element
     */
    ITextStyle defaultTextStyle();

    /**
     * Create a default style for the item element
     */
    IItemStyle defaultItemStyle();

    /**
     * Create a default style for the entity element
     */
    IEntityStyle defaultEntityStyle();

    /**
     * Create a default style for the icon element
     */
    IIconStyle defaultIconStyle();

    /**
     * Create an icon. If u and v are -1 then the default texture atlas is used
     * and TheOneProbe will try to find the resource location on that atlas
     */
    IProbeInfo icon(ResourceLocation icon, int u, int v, int w, int h, IIconStyle style);
    IProbeInfo icon(ResourceLocation icon, int u, int v, int w, int h);

    /**
     * entityName can be an old-style entity name (like 'Zombie') or the string
     * representation of a resourcelocation (like 'minecraft:zombie')
     */
    IProbeInfo entity(String entityName, IEntityStyle style);
    IProbeInfo entity(String entityName);
    IProbeInfo entity(Entity entity, IEntityStyle style);
    IProbeInfo entity(Entity entity);

    /**
     * Note that you can include TextStyleClass info in the given text which
     * will be translated to the right style client-side. You can also
     * include STARTLOC/ENDLOC tags to force translation to localized
     * data on the client
     */
    IProbeInfo text(String text, ITextStyle style);
    IProbeInfo text(String text);

    IProbeInfo item(ItemStack stack, IItemStyle style);
    IProbeInfo item(ItemStack stack);

    /**
     * A localized name of the stack
     */
    IProbeInfo itemLabel(ItemStack stack, ITextStyle style);
    IProbeInfo itemLabel(ItemStack stack);

    /**
     * This creates a progress bar of 100 width
     */
    IProbeInfo progress(int current, int max, IProgressStyle style);
    IProbeInfo progress(int current, int max);
    IProbeInfo progress(long current, long max, IProgressStyle style);
    IProbeInfo progress(long current, long max);

    /**
     * Create a new horizontal probe info as a child of this one. Note that the returned
     * probe info is the new horizontal layout and not this one!
     */
    IProbeInfo horizontal(ILayoutStyle style);
    IProbeInfo horizontal();

    /**
     * Create a new vertical probe info as a child of this one. Note that the returned
     * probe info is the new horizontal layout and not this one!
     */
    IProbeInfo vertical(ILayoutStyle style);
    IProbeInfo vertical();

    /**
     * Add a custom element. Make sure the factory for this element is properly registered.
     */
    IProbeInfo element(IElement element);
}
