package com.creativemd.littletiles.common.tileentity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.tile.LittleTile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileList implements List<LittleTile> {
	
	private final CopyOnWriteArrayList<LittleTile> content = new CopyOnWriteArrayList<LittleTile>();
	private final CopyOnWriteArrayList<LittleTile> ticking = new CopyOnWriteArrayList<LittleTile>();
	
	private final CopyOnWriteArrayList<LittleStructure> activeStructures = new CopyOnWriteArrayList<>();
	
	private int attributes = LittleStructureAttribute.NONE;
	
	@SideOnly(Side.CLIENT)
	private CopyOnWriteArrayList<LittleTile> render;
	
	private int collisionChecks = 0;
	
	private final boolean client;
	
	private final TileEntityLittleTiles te;
	
	public TileList(TileEntityLittleTiles te, boolean client) {
		this.te = te;
		this.client = client;
		if (client)
			render = new CopyOnWriteArrayList<LittleTile>();
	}
	
	@SideOnly(Side.CLIENT)
	public List<LittleTile> renderTiles() {
		return render;
	}
	
	@Override
	public boolean contains(Object o) {
		return content.contains(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return content.containsAll(c);
	}
	
	public Iterable<LittleStructure> structures() {
		return activeStructures;
	}
	
	public Iterable<LittleStructure> allStructures() {
		return new Iterable<LittleStructure>() {
			
			@Override
			public Iterator<LittleStructure> iterator() {
				return new Iterator<LittleStructure>() {
					
					public Iterator<LittleTile> iterator = content.iterator();
					public List<LittleStructure> usedStructures = new ArrayList<>();
					public LittleStructure next;
					
					{
						findNext();
					}
					
					public void findNext() {
						while (iterator.hasNext()) {
							LittleTile tile = iterator.next();
							if (tile.isChildOfStructure()) {
								LittleStructure structure = tile.connection.getStructure(te.getWorld());
								if (usedStructures.contains(structure))
									continue;
								usedStructures.add(structure);
								next = structure;
								return;
							}
						}
						
						next = null;
					}
					
					@Override
					public boolean hasNext() {
						return next != null;
					}
					
					@Override
					public LittleStructure next() {
						LittleStructure toReturn = next;
						findNext();
						return toReturn;
					}
				};
			}
		};
	}
	
	public Iterable<LittleStructure> structures(int attribute) {
		return new Iterable<LittleStructure>() {
			
			@Override
			public Iterator<LittleStructure> iterator() {
				if (LittleStructureAttribute.listener(attribute)) {
					return new Iterator<LittleStructure>() {
						
						public Iterator<LittleTile> iterator = content.iterator();
						public List<LittleStructure> usedStructures = new ArrayList<>();
						public LittleStructure next;
						
						{
							findNext();
						}
						
						public void findNext() {
							while (iterator.hasNext()) {
								LittleTile tile = iterator.next();
								if (tile.isChildOfStructure()) {
									LittleStructure structure = tile.connection.getStructure(te.getWorld());
									if (structure != null && (structure.getAttribute() & attribute) != 0) {
										if (usedStructures.contains(structure))
											continue;
										usedStructures.add(structure);
										next = structure;
										return;
									}
								}
							}
							
							next = null;
						}
						
						@Override
						public boolean hasNext() {
							return next != null;
						}
						
						@Override
						public LittleStructure next() {
							LittleStructure toReturn = next;
							findNext();
							return toReturn;
						}
					};
				}
				return new Iterator<LittleStructure>() {
					
					public Iterator<LittleStructure> iterator = activeStructures.iterator();
					public LittleStructure next;
					
					{
						findNext();
					}
					
					public void findNext() {
						while (iterator.hasNext()) {
							LittleStructure structure = iterator.next();
							if ((structure.getAttribute() & attribute) != 0) {
								next = structure;
								return;
							}
						}
						
						next = null;
					}
					
					@Override
					public boolean hasNext() {
						return next != null;
					}
					
					@Override
					public LittleStructure next() {
						LittleStructure toReturn = next;
						findNext();
						return toReturn;
					}
				};
			}
			
		};
	}
	
	public List<LittleTile> tickingTiles() {
		return ticking;
	}
	
	@Override
	public int size() {
		return content.size();
	}
	
	@Override
	public boolean isEmpty() {
		return content.isEmpty();
	}
	
	public boolean hasTicking() {
		return !ticking.isEmpty() || LittleStructureAttribute.ticking(attributes);
	}
	
	public boolean hasRendered() {
		if (LittleStructureAttribute.tickRendering(attributes))
			return true;
		
		if (client)
			return !render.isEmpty();
		
		for (LittleTile tile : content)
			if (tile.needCustomRendering())
				return true;
			
		return false;
	}
	
	public boolean checkCollision() {
		return collisionChecks > 0;
	}
	
	public LittleTile first() {
		return isEmpty() ? null : content.get(0);
	}
	
	private void reset() {
		ticking.clear();
		if (client)
			render.clear();
		collisionChecks = 0;
		activeStructures.clear();
		attributes = LittleStructureAttribute.NONE;
	}
	
	@Override
	public boolean add(LittleTile e) {
		content.add(e);
		added(e);
		return true;
	}
	
	@Override
	public void add(int index, LittleTile element) {
		content.add(index, element);
		added(element);
	}
	
	@Override
	public boolean addAll(Collection<? extends LittleTile> c) {
		if (content.addAll(c)) {
			for (LittleTile tile : c)
				added(tile);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends LittleTile> c) {
		if (content.addAll(index, c)) {
			for (LittleTile tile : c)
				added(tile);
			return true;
		}
		return false;
	}
	
	private void added(LittleTile tile) {
		if (tile.shouldTick())
			ticking.add(tile);
		if (client && tile.needCustomRendering())
			render.add(tile);
		if (tile.shouldCheckForCollision())
			collisionChecks++;
		if (tile.isChildOfStructure() && !tile.connection.isLink() && LittleStructureAttribute.active(tile.connection.getAttribute())) {
			activeStructures.add(tile.connection.getStructureWithoutLoading());
			attributes |= tile.connection.getAttribute();
		}
	}
	
	@Override
	public LittleTile remove(int index) {
		LittleTile tile = content.remove(index);
		if (tile != null)
			removed(tile);
		return tile;
	}
	
	private void removed(LittleTile tile) {
		ticking.remove(tile);
		if (client)
			render.remove(tile);
		if (tile.shouldCheckForCollision())
			collisionChecks--;
		if (tile.isChildOfStructure() && !tile.connection.isLink() && LittleStructureAttribute.active(tile.connection.getAttribute())) {
			activeStructures.remove(tile.connection.getStructureWithoutLoading());
			attributes = LittleStructureAttribute.NONE;
			for (LittleStructure structure : activeStructures)
				attributes |= structure.getAttribute();
		}
	}
	
	@Override
	public boolean remove(Object o) {
		if (content.remove(o)) {
			removed((LittleTile) o);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean removeIf(Predicate<? super LittleTile> filter) {
		if (content.removeIf(filter)) {
			reset();
			for (LittleTile tile : content)
				added(tile);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		for (Object object : c)
			if (remove(object))
				changed = true;
		return changed;
	}
	
	@Override
	public void clear() {
		content.clear();
		reset();
	}
	
	@Override
	public Iterator<LittleTile> iterator() {
		return content.iterator();
	}
	
	@Override
	public Object[] toArray() {
		return content.toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] a) {
		return content.toArray(a);
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		if (content.retainAll(c)) {
			reset();
			for (LittleTile tile : content)
				added(tile);
			return true;
		}
		return false;
	}
	
	@Override
	public LittleTile get(int index) {
		return content.get(index);
	}
	
	@Override
	public LittleTile set(int index, LittleTile element) {
		LittleTile tile = remove(index);
		removed(tile);
		content.set(index, element);
		added(element);
		return tile;
	}
	
	@Override
	public int indexOf(Object o) {
		return content.indexOf(o);
	}
	
	@Override
	public int lastIndexOf(Object o) {
		return content.lastIndexOf(o);
	}
	
	@Override
	public ListIterator<LittleTile> listIterator() {
		return content.listIterator();
	}
	
	@Override
	public ListIterator<LittleTile> listIterator(int index) {
		return content.listIterator(index);
	}
	
	@Override
	public void sort(Comparator<? super LittleTile> c) {
		content.sort(c);
	}
	
	@Override
	public void replaceAll(UnaryOperator<LittleTile> operator) {
		content.replaceAll(operator);
		reset();
		for (LittleTile tile : content)
			added(tile);
	}
	
	@Override
	public List<LittleTile> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Spliterator<LittleTile> spliterator() {
		throw new UnsupportedOperationException();
	}
	
}