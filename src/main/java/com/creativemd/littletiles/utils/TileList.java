package com.creativemd.littletiles.utils;

import java.util.ArrayList;
import java.util.Collection;

public class TileList<E> extends ArrayList<E> {
	
	public TileList(int paramInt)
	{
		super(paramInt);
	}
	
	public TileList()
	{
		super();
	}
	  
	public TileList(Collection<? extends E> paramCollection)
	{
		super(paramCollection);
	}
	
	@Override
	public E get(int paramInt)
	{
		if (paramInt < size()) {
			return super.get(paramInt);
		}
		return null;
	}
}
