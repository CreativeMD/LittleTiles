package com.creativemd.littletiles.common.utils.animation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;

public abstract class ValueTimeline {
	
	private static List<Class<? extends ValueTimeline>> valueTimelineTypes = new ArrayList<>();
	
	public static void registerValueTimelineType(Class<? extends ValueTimeline> type) {
		valueTimelineTypes.add(type);
	}
	
	public static Class<? extends ValueTimeline> getType(int id) {
		if (id < -1 || id >= valueTimelineTypes.size())
			throw new RuntimeException("Invalid id " + id);
		return valueTimelineTypes.get(id);
	}
	
	public static int getId(Class<? extends ValueTimeline> type) {
		int index = valueTimelineTypes.indexOf(type);
		if (index == -1)
			throw new RuntimeException("Invalid type " + type.getName());
		return index;
	}
	
	public static ValueTimeline read(int[] array) {
		if (array.length < 2)
			throw new RuntimeException("Invalid array size=" + array.length);
		Class<? extends ValueTimeline> type = getType(array[0]);
		try {
			ValueTimeline timeline = type.getConstructor().newInstance();
			int points = array[1];
			for (int i = 0; i < points; i++) {
				timeline.points.add(array[2 + i * 3], Double.longBitsToDouble((((long) array[3 + i * 3]) << 32) | (array[4 + i * 3] & 0xffffffffL)));
			}
			timeline.readAdditionalData(array, 2 + points * 3);
			return timeline;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static ValueTimeline create(int id, PairList<Integer, ?> points) {
		if (points == null)
			return null;
		
		try {
			ValueTimeline timeline = getType(id).getConstructor().newInstance();
			for (Pair<Integer, ?> pair : points) {
				double value;
				if (pair.value instanceof Integer)
					value = (Integer) pair.value;
				else
					value = (Double) pair.value;
				timeline.addPoint(pair.key, value);
			}
			return timeline;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	static {
		registerValueTimelineType(LinearTimeline.class);
	}
	
	protected PairList<Integer, Double> points = new PairList<>();
	
	public PairList<Integer, Double> getPointsCopy() {
		return new PairList<>(points);
	}
	
	public PairList<Integer, Integer> getRoundedPointsCopy() {
		PairList<Integer, Integer> newPoints = new PairList<>();
		for (Pair<Integer, Double> pair : points) {
			newPoints.add(pair.key, (int) (double) pair.value);
		}
		return newPoints;
	}
	
	public ValueTimeline addPoints(PairList<Integer, Double> points) {
		for (Pair<Integer, Double> point : points) {
			addPoint(point.key, point.value);
		}
		return this;
	}
	
	public ValueTimeline addPoint(Integer tick, Double value) {
		for (int i = 0; i < points.size(); i++) {
			if (points.get(i).key > tick) {
				points.add(i, new Pair<Integer, Double>(tick, value));
				return this;
			}
		}
		points.add(tick, value);
		return this;
	}
	
	public ValueTimeline factor(double factor) {
		for (Pair<Integer, Double> point : points) {
			point.value *= factor;
		}
		return this;
	}
	
	public abstract double value(int tick);
	
	public double last() {
		if (points.isEmpty())
			return 0;
		return points.getLast().value;
	}
	
	public int[] write() {
		int[] data = new int[2 + points.size() * 3 + getAdditionalDataSize()];
		data[0] = getId(this.getClass());
		data[1] = points.size();
		for (int i = 0; i < points.size(); i++) {
			Pair<Integer, Double> pair = points.get(i);
			data[i * 3 + 2] = pair.key;
			long point = Double.doubleToLongBits(pair.value);
			data[i * 3 + 3] = (int) (point >> 32);
			data[i * 3 + 4] = (int) point;
		}
		writeAdditionalData(data, 2 + points.size() * 3);
		return data;
	}
	
	protected abstract int getAdditionalDataSize();
	
	protected abstract void writeAdditionalData(int[] array, int index);
	
	protected abstract void readAdditionalData(int[] array, int index);
	
	protected abstract void invertData(ValueTimeline original);
	
	public ValueTimeline copy() {
		return read(write());
	}
	
	public void flip() {
		for (Pair<Integer, Double> pair : points) {
			pair.value = -pair.value;
		}
	}
	
	public ValueTimeline invert(int duration) {
		try {
			ValueTimeline timeline = getClass().getConstructor().newInstance();
			for (int i = points.size() - 1; i >= 0; i--) {
				Pair<Integer, Double> pair = points.get(i);
				timeline.points.add(duration - pair.key, pair.value);
			}
			timeline.invertData(this);
			return timeline;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static class LinearTimeline extends ValueTimeline {
		
		@Override
		public double value(int tick) {
			int higher = points.size();
			for (int i = 0; i < points.size(); i++) {
				int otherTick = points.get(i).key;
				if (otherTick == tick)
					return points.get(i).value;
				if (otherTick > tick) {
					higher = i;
					break;
				}
			}
			
			if (higher == 0 || higher == points.size())
				return points.get(higher == 0 ? 0 : points.size() - 1).value;
			
			Pair<Integer, Double> before = points.get(higher - 1);
			Pair<Integer, Double> after = points.get(higher);
			double percentage = (double) (tick - before.key) / (after.key - before.key);
			return (after.value - before.value) * percentage + before.value;
		}
		
		@Override
		protected int getAdditionalDataSize() {
			return 0;
		}
		
		@Override
		protected void writeAdditionalData(int[] array, int index) {
			
		}
		
		@Override
		protected void readAdditionalData(int[] array, int index) {
			
		}
		
		@Override
		protected void invertData(ValueTimeline original) {
			
		}
		
	}
	
	public void offset(int offset) {
		PairList<Integer, Double> newPoints = new PairList<>();
		for (Pair<Integer, Double> point : points)
			newPoints.add(point.key + offset, point.value);
	}
	
}
