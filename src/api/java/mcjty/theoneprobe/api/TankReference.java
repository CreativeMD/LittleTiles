package mcjty.theoneprobe.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

public final class TankReference {
    private final int capacity;
    private final int stored;
    private final FluidStack[] fluids;

    public TankReference(int capacity, int stored, FluidStack... fluids) {
        this.capacity = capacity;
        this.stored = stored;
        this.fluids = fluids;
    }

    public TankReference(FriendlyByteBuf buffer) {
        capacity = buffer.readInt();
        stored = buffer.readInt();
        fluids = new FluidStack[buffer.readInt()];
        for (int i = 0; i < fluids.length; i++) {
            fluids[i] = buffer.readFluidStack();
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public int getStored() {
        return stored;
    }

    public FluidStack[] getFluids() {
        return fluids;
    }

    /// Simple Self Simulated Tank or just a fluid display
    public static TankReference createSimple(int capacity, FluidStack fluid) {
        return new TankReference(capacity, fluid.getAmount(), fluid);
    }

    /// Simple Tank like FluidTank
    public static TankReference createTank(IFluidTank tank) {
        return new TankReference(tank.getCapacity(), tank.getFluidAmount(), tank.getFluid());
    }

    /// Any Fluid Handler, but Squashes all the fluids into 1 Progress Bar
    public static TankReference createHandler(IFluidHandler handler) {
        int capacity = 0;
        int stored = 0;
        FluidStack[] fluids = new FluidStack[handler.getTanks()];
        for (int i = 0; i < fluids.length; i++) {
            capacity += handler.getTankCapacity(i);
            FluidStack fluid = handler.getFluidInTank(i);
            fluids[i] = fluid;
            stored += fluid.getAmount();
        }
        return new TankReference(capacity, stored, fluids);
    }

    /// Any Fluid Handler but splits each internal Tank into its own Progress Bar
    public static TankReference[] createSplitHandler(IFluidHandler handler) {
        TankReference[] references = new TankReference[handler.getTanks()];
        for (int i = 0; i < references.length; i++) {
            FluidStack fluid = handler.getFluidInTank(i);
            references[i] = new TankReference(handler.getTankCapacity(i), fluid.getAmount(), fluid);
        }
        return references;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(capacity);
        buffer.writeInt(stored);
        buffer.writeInt(fluids.length);
		for (FluidStack fluid : fluids) {
			buffer.writeFluidStack(fluid);
		}
    }
}
