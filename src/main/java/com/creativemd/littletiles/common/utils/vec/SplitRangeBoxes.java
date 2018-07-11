package com.creativemd.littletiles.common.utils.vec;

import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.common.utils.math.RangedBitSet;
import com.creativemd.creativecore.common.utils.math.RangedBitSet.BitRange;

public class SplitRangeBoxes implements Iterable<SplitRangeBoxes.SplitRangeBox> {
	
	protected List<BitRange> xList;
	protected List<BitRange> yList;
	protected List<BitRange> zList;
	
	public SplitRangeBoxes(RangedBitSet x, RangedBitSet y, RangedBitSet z)
	{
		this.xList = x.getRanges();
		this.yList = y.getRanges();
		this.zList = z.getRanges();
	}
	
	@Override
	public Iterator<SplitRangeBox> iterator() {
		
		SplitRangeBox box = new SplitRangeBox();
		
		return new Iterator<SplitRangeBoxes.SplitRangeBox>() {
			
			public int x = 0;
			public int y = 0;
			public int z = 0;
			
			public int total = xList.size() * yList.size() * zList.size();
			public int current = 0;
			
			@Override
			public boolean hasNext() {
				
				return current < total;
			}

			@Override
			public SplitRangeBox next() {
				box.set(xList.get(x), yList.get(y),  zList.get(z));
				z++;
				if(z >= zList.size())
				{
					y++;
					z = 0;
					if(y >= yList.size())
					{
						x++;
						y = 0;
					}
				}
				current++;
				return box;
			}
		};
	}
	
	public static class SplitRangeBox {
		
		public BitRange x;
		public BitRange y;
		public BitRange z;
		public boolean value;
		
		public void set(BitRange x, BitRange y, BitRange z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			this.value = x.value && y.value && z.value;
		}
		
	}
	
}
