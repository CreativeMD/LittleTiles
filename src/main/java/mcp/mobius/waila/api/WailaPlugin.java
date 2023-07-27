package mcp.mobius.waila.api;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Used to mark a class as a Waila plugin. Detected at runtime automatically.
 * <p>
 * Classes annotated with this must implement {@link IWailaPlugin}.
 * <p>
 * To other HUD mods: If you wish to add support for mods that have native support for Waila, use Forge's ASMDataTable
 * (accessible via {@link FMLPreInitializationEvent#getAsmData()}) to detect this annotation. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WailaPlugin {
    
    /** @return the Mod ID required for this plugin to load. If blank, it will load no matter what. */
    String value() default "";
}
