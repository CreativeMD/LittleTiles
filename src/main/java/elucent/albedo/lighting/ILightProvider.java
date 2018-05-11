package elucent.albedo.lighting;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ILightProvider {
	@SideOnly(Side.CLIENT)
	public Light provideLight();
}