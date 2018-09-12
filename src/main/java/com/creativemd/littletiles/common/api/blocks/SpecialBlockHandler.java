package com.creativemd.littletiles.common.api.blocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.block.Block;

public class SpecialBlockHandler {

	private static int layers = 0;

	public static abstract class BlockSelector {

		public abstract boolean isBlock(Block block, int meta);

		public abstract boolean equals(Object object);

		public abstract int getLayer();

	}

	public static class BlockSelectorClass extends BlockSelector {

		public final Class<? extends Block> clazz;

		public BlockSelectorClass(Class<? extends Block> clazz) {
			this.clazz = clazz;
		}

		@Override
		public boolean isBlock(Block block, int meta) {
			return this.clazz.isInstance(block);
		}

		@Override
		public boolean equals(Object object) {
			if (object instanceof BlockSelectorClass)
				return ((BlockSelectorClass) object).clazz == clazz;
			return false;
		}

		@Override
		public int getLayer() {
			return 1;
		}

	}

	public static class BlockSelectorBasic extends BlockSelector {

		public final Block block;

		public BlockSelectorBasic(Block block) {
			this.block = block;
		}

		@Override
		public boolean isBlock(Block block, int meta) {
			return this.block == block;
		}

		@Override
		public boolean equals(Object object) {
			if (object instanceof BlockSelectorBasic)
				return ((BlockSelectorBasic) object).block == this.block;
			return false;
		}

		@Override
		public int getLayer() {
			return 2;
		}

	}

	public static class BlockSelectorAdvanced extends BlockSelectorBasic {

		public final int meta;

		public BlockSelectorAdvanced(Block block, int meta) {
			super(block);
			this.meta = meta;
		}

		@Override
		public boolean isBlock(Block block, int meta) {
			return super.isBlock(block, meta) && this.meta == meta;
		}

		@Override
		public boolean equals(Object object) {
			if (object instanceof BlockSelectorAdvanced)
				return super.equals(object) && ((BlockSelectorAdvanced) object).meta == meta;
			return false;
		}

		@Override
		public int getLayer() {
			return 3;
		}

	}

	public static HashMap<BlockSelector, ISpecialBlockHandler> specialHandlers = new HashMap<>();

	public static ISpecialBlockHandler getSpecialBlockHandler(Block block, int meta) {
		ISpecialBlockHandler[] specialLayers = new ISpecialBlockHandler[layers];
		for (Iterator<Entry<BlockSelector, ISpecialBlockHandler>> iterator = specialHandlers.entrySet().iterator(); iterator.hasNext();) {
			Entry<BlockSelector, ISpecialBlockHandler> entry = iterator.next();
			if (entry.getKey().isBlock(block, meta)) {
				if (entry.getKey().getLayer() < layers)
					specialLayers[entry.getKey().getLayer()] = entry.getValue();
				else
					return entry.getValue();
			}
		}
		for (int i = specialLayers.length - 1; i >= 0; i--) {
			if (specialLayers[i] != null)
				return specialLayers[i];
		}

		if (block instanceof ISpecialBlockHandler)
			return (ISpecialBlockHandler) block;
		return null;
	}

	public static void registerSpecialHandler(Class<? extends Block> clazz, ISpecialBlockHandler handler) {
		registerSpecialHandler(new BlockSelectorClass(clazz), handler);
	}

	public static void registerSpecialHandler(Block block, int meta, ISpecialBlockHandler handler) {
		registerSpecialHandler(new BlockSelectorAdvanced(block, meta), handler);
	}

	public static void registerSpecialHandler(Block block, ISpecialBlockHandler handler) {
		registerSpecialHandler(new BlockSelectorBasic(block), handler);
	}

	public static void registerSpecialHandler(BlockSelector selector, ISpecialBlockHandler handler) {
		layers = Math.max(layers, selector.getLayer());
		specialHandlers.put(selector, handler); // It is intentional that you can override existing SpecialHandlers
	}

}
