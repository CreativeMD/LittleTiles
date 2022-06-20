package mcjty.theoneprobe.api;

import java.util.Collection;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

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

    /**
     * Use STARTLOC/ENDLOC in your strings for localization on the client
     * Note that usage of this is deprecated. Use TranslationTextComponent instead
     */
    String STARTLOC = "{*";
    String ENDLOC = "*}";

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
     * Send (possibly formatted) text to the client. Note that you can use
     * CompoundText as a conveniance and giving you the option to use TextStyleClass
     * for your text
     */
    IProbeInfo text(Component text);
    IProbeInfo text(Component text, ITextStyle style);
    IProbeInfo mcText(Component text);
    IProbeInfo mcText(Component text, ITextStyle style);
    default IProbeInfo text(CompoundText text) { return text(text.get()); }
    default IProbeInfo text(CompoundText text, ITextStyle style) { return text(text.get(), style); }
    default IProbeInfo text(String text) { return mcText(Component.translatable(text)); }
    default IProbeInfo text(String text, Object...args) { return mcText(Component.translatable(text, args)); }
    default IProbeInfo text(String text, ITextStyle style) { return mcText(Component.translatable(text), style); }
    default IProbeInfo text(String text, ITextStyle style, Object...args) { return mcText(Component.translatable(text, args), style); }
    
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
     * This creates a Tank Progress bar of 100 width with Fluid Icon Rendering
     */
    default IProbeInfo tankSimple(int capacity, FluidStack fluid) { return tank(TankReference.createSimple(capacity, fluid));}
    default IProbeInfo tank(IFluidTank tank) { return tank(TankReference.createTank(tank));}
    default IProbeInfo tankHandler(IFluidHandler handler) { return tank(TankReference.createHandler(handler));}
    IProbeInfo tank(TankReference tank);
    
    default IProbeInfo tankSimple(int capacity, FluidStack fluid, IProgressStyle style) { return tank(TankReference.createSimple(capacity, fluid), style);}
    default IProbeInfo tank(IFluidTank tank, IProgressStyle style) { return tank(TankReference.createTank(tank), style);}
    default IProbeInfo tankHandler(IFluidHandler handler, IProgressStyle style) { return tank(TankReference.createHandler(handler), style);}
    IProbeInfo tank(TankReference tank, IProgressStyle style);
    
    IProbeInfo padding(int width, int height);
    
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
    /**
     * Bulk adding methods for cached elements to be simpler added.
     * Stuff that never changes
     */
    default IProbeInfo elements(IElement...elements) {
    	for(IElement element : elements) {
    		element(element);
    	}
    	return this;
    }
    /**
     * Bulk adding methods for cached elements to be simpler added.
     * Stuff that never changes
     */
    default IProbeInfo elements(Collection<IElement> elements) {
    	for(IElement element : elements) {
    		element(element);
    	}
    	return this;
    }
    
    
    /**
     * Allows access to the elements stored in the Probe Info via the interface.
     * Removes the need of Casting to implementation just to achieve what the user wants.
     * Or for simplifying counting methods if checks happen if a custom Panel has elements or not.
     */
    List<IElement> getElements();
}
