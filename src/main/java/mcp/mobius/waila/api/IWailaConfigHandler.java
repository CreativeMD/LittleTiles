package mcp.mobius.waila.api;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Set;

/** Read-only interface for Waila internal config storage.<br>
 * An instance of this interface is passed to most of Waila callbacks as a way to change the behavior depending on client settings.
 *
 * @author ProfMobius */
public interface IWailaConfigHandler {
    /** Returns a set of all the currently loaded modules in the config handler.
     *
     * @return a set of all the currently loaded modules in the config handler. */
    @Nonnull
    Set<String> getModuleNames();
    
    /** Returns all the currently available options for a given module
     *
     * @param modName
     *            Module name
     * @return all the currently available options for a given module */
    @Nonnull
    HashMap<String, String> getConfigKeys(String modName);
    
    /** Returns the current value of an option (true/false) with a default value if not set.
     *
     * @param key
     *            Option to lookup
     * @param defvalue
     *            Default values
     * @return Value of the option or defvalue if not set. */
    boolean getConfig(String key, boolean defvalue);
    
    /** Returns the current value of an option (true/false) with a default value true if not set
     *
     * @param key
     *            Option to lookup
     * @return Value of the option or true if not set. */
    boolean getConfig(String key);
}
