package com.creativemd.littletiles.common.world;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.utils.HashMapList;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class WorldChunkedTileEntityList implements List<TileEntity> {
	
	public static ChunkPos getChunkPos(TileEntity te)
	{
		return new ChunkPos(te.getPos());
	}
	
	public HashMapList<ChunkPos, TileEntity> content = new HashMapList<ChunkPos, TileEntity>();
	
	public boolean removeChunk(World world, Chunk chunk, boolean notifyTes)
	{
		if(notifyTes)
		{
			for (TileEntity tileentity : chunk.getTileEntityMap().values())
	        {
	            world.markTileEntityForRemoval(tileentity);
	        }
		}
		
		return content.removeKey(chunk.getPos());
	}

	@Override
	public int size() {
		return content.sizeOfValues();
	}

	@Override
	public boolean isEmpty() {
		return content.isEmpty();
	}

	@Override
	public boolean contains(Object paramObject) {
		if(paramObject instanceof TileEntity)
		{
			ArrayList<TileEntity> tes = content.getValues(getChunkPos((TileEntity) paramObject));
			if(tes != null)
				return tes.contains(paramObject);
		}
		return false;
	}

	@Override
	public Iterator<TileEntity> iterator() {
		List<Iterator<TileEntity>> iterators = new ArrayList<>();
		for (Iterator<ArrayList<TileEntity>> iterator = content.getValues().iterator(); iterator.hasNext();) {
			ArrayList<TileEntity> list = iterator.next();
			iterators.add(list.iterator());
		}
		
		return new Iterator<TileEntity>() {
			
			public int index = -1;

			@Override
			public boolean hasNext() {
				int index = this.index+1;
				while(index < iterators.size())
				{
					Iterator<TileEntity> iterator = iterators.get(index);
					if(iterator.hasNext())
						return true;
					index++;
				}
				return false;
			}

			@Override
			public TileEntity next() {
				this.index++;
				while(index < iterators.size())
				{
					Iterator<TileEntity> iterator = iterators.get(index);
					if(iterator.hasNext())
						return iterator.next();;
					index++;
				}
				return null;
			}
		};
	}

	@Override
	public Object[] toArray() {
		Object[] array = new Object[content.sizeOfValues()];
		int i = 0;
		for (Iterator<TileEntity> iterator = iterator(); iterator.hasNext();) {
			array[i] = iterator.next();
			i++;
		}
		return array;
	}

	@Override
	public <T> T[] toArray(T[] paramArrayOfT) {
		return null;
	}

	@Override
	public boolean add(TileEntity paramE) {
		content.add(getChunkPos(paramE), paramE);
		return true;
	}

	@Override
	public boolean remove(Object paramObject) {
		if(paramObject instanceof TileEntity)
			return content.removeValue(getChunkPos((TileEntity) paramObject), (TileEntity) paramObject);
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> paramCollection) {
		for (Iterator iterator = paramCollection.iterator(); iterator.hasNext();) {
			Object object = (Object) iterator.next();
			if(!contains(object))
				return false;
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends TileEntity> paramCollection) {
		boolean result = true;
		for (Iterator iterator = paramCollection.iterator(); iterator.hasNext();) {
			TileEntity tileEntity = (TileEntity) iterator.next();
			if(!add(tileEntity))
				result = false;
		}
		return result;
	}

	@Override
	public boolean addAll(int paramInt, Collection<? extends TileEntity> paramCollection) {
		return addAll(paramCollection); //Ordering does not work
	}

	@Override
	public boolean removeAll(Collection<?> paramCollection) {
		boolean result = true;
		for (Iterator iterator = paramCollection.iterator(); iterator.hasNext();) {
			TileEntity tileEntity = (TileEntity) iterator.next();
			if(!remove(tileEntity))
				result = false;
		}
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> paramCollection) {
		//NOT SUPPORTED!!!!
		return false;
	}

	@Override
	public void clear() {
		content.clear();
	}

	@Override
	public TileEntity get(int paramInt) {
		// Ouch this should not happen, bad performance can be expected!!!
		
		int currentIndex = 0;
		for (Iterator<ArrayList<TileEntity>> iterator = content.getValues().iterator(); iterator.hasNext();) {
			ArrayList<TileEntity> type = iterator.next();
			if(currentIndex + type.size() < paramInt)
				currentIndex += type.size();
			else
				return type.get(paramInt-currentIndex);
		}
		
		return null;
	}

	@Override
	public TileEntity set(int paramInt, TileEntity paramE) {
		// Ouch this should not happen, bad performance can be expected!!!
		
		int currentIndex = 0;
		for (Iterator<ArrayList<TileEntity>> iterator = content.getValues().iterator(); iterator.hasNext();) {
			ArrayList<TileEntity> type = iterator.next();
			if(currentIndex + type.size() < paramInt)
				currentIndex += type.size();
			else
				return type.set(paramInt-currentIndex, paramE);
		}
		
		return null;
	}

	@Override
	public void add(int paramInt, TileEntity paramE) {
		add(paramE); //Sorting is not allowed
	}

	@Override
	public TileEntity remove(int paramInt) {
		// Ouch this should not happen, bad performance can be expected!!!
		int currentIndex = 0;
		for (Iterator<ArrayList<TileEntity>> iterator = content.getValues().iterator(); iterator.hasNext();) {
			ArrayList<TileEntity> type = iterator.next();
			if(currentIndex + type.size() < paramInt)
				currentIndex += type.size();
			else
				return type.remove(paramInt-currentIndex);
		}
		return null;
	}

	@Override
	public int indexOf(Object paramObject) {
		// Ouch this should not happen, bad performance can be expected!!!
		if(!(paramObject instanceof TileEntity))
			return -1;
		
		int currentIndex = 0;
		ChunkPos pos = getChunkPos((TileEntity) paramObject);
		if(content.getValues(pos) == null)
			return -1;
		
		for (Iterator<Entry<ChunkPos, ArrayList<TileEntity>>> iterator = content.entrySet().iterator(); iterator.hasNext();) {
			Entry<ChunkPos, ArrayList<TileEntity>> type = iterator.next();
			if(type.getKey().equals(pos))
			{
				int index = type.getValue().indexOf(paramObject);
				if(index != -1)
					return index+currentIndex;
				return -1;
			}else
				currentIndex += type.getValue().size();
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object paramObject) {
		// Ouch this should not happen, bad performance can be expected!!!
				if(!(paramObject instanceof TileEntity))
					return -1;
				
				int currentIndex = 0;
				ChunkPos pos = getChunkPos((TileEntity) paramObject);
				if(content.getValues(pos) == null)
					return -1;
				
				for (Iterator<Entry<ChunkPos, ArrayList<TileEntity>>> iterator = content.entrySet().iterator(); iterator.hasNext();) {
					Entry<ChunkPos, ArrayList<TileEntity>> type = iterator.next();
					if(type.getKey().equals(pos))
					{
						int index = type.getValue().lastIndexOf(paramObject);
						if(index != -1)
							return index+currentIndex;
						return -1;
					}else
						currentIndex += type.getValue().size();
				}
				return -1;
	}

	@Override
	public ListIterator<TileEntity> listIterator() {
		//Unsupported
		return null;
	}

	@Override
	public ListIterator<TileEntity> listIterator(int paramInt) {
		//Unsupported
		return null;
	}

	@Override
	public List<TileEntity> subList(int paramInt1, int paramInt2) {
		//Unsupported
		return null;
	}
	
	

}
