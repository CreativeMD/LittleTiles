package mcp.mobius.waila.api;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.Dimension;

@ParametersAreNonnullByDefault
public interface IWailaTooltipRenderer {
    /** @param params
     *            Array of string parameters as passed to the RENDER arg in the tooltip ({rendername,param1,param2,...})
     * @param accessor
     *            A global accessor for TileEntities and Entities
     * @return Dimension of the reserved area */
    @Nonnull
    Dimension getSize(String[] params, IWailaCommonAccessor accessor);
    
    /** Draw method for the renderer. The GL matrice is automatically moved to the top left of the reserved zone.<br>
     * All calls should be relative to (0,0)
     *
     * @param params
     *            Array of string parameters as passed to the RENDER arg in the tooltip ({rendername,param1,param2,...})
     * @param accessor
     *            A global accessor for TileEntities and Entities */
    void draw(String[] params, IWailaCommonAccessor accessor);
}
