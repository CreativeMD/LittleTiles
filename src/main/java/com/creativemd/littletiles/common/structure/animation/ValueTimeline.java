package com.creativemd.littletiles.common.structure.animation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.math.interpolation.HermiteInterpolation.Tension;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;

public abstract class ValueTimeline {
	
	private static List<Class<? extends ValueTimeline>> valueTimelineTypes = new ArrayList<>();
	public static String[] interpolationTypes = new String[] { "linear", "cosine", "cubic", "hermite" };
	
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
	
	public static ValueTimeline create(int id) {
		try {
			return getType(id).getConstructor().newInstance();
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
		registerValueTimelineType(CosineTimeline.class);
		registerValueTimelineType(CubicTimeline.class);
		registerValueTimelineType(HermiteTimeline.class);
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
	
	public double value(int tick) {
		if (tick < 0)
			return 0;
		
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
		return valueAt(percentage, before, higher - 1, after, higher);
	}
	
	public abstract double valueAt(double mu, Pair<Integer, Double> before, int pointIndex, Pair<Integer, Double> after, int pointIndexNext);
	
	public double first(AnimationKey key) {
		if (points.isEmpty())
			return key.getDefault();
		return points.getFirst().value;
	}
	
	public double last(AnimationKey key) {
		if (points.isEmpty())
			return key.getDefault();
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
	
	protected void invertData(ValueTimeline original) {
		
	}
	
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
				timeline.addPoint(duration - pair.key, pair.value);
			}
			timeline.invertData(this);
			return timeline;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static class LinearTimeline extends ValueTimeline {
		
		public LinearTimeline() {
			
		}
		
		@Override
		public double valueAt(double mu, Pair<Integer, Double> before, int pointIndex, Pair<Integer, Double> after, int pointIndexNext) {
			return (after.value - before.value) * mu + before.value;
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
	}
	
	public static class CosineTimeline extends ValueTimeline {
		
		public CosineTimeline() {
			
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
		public double valueAt(double mu, Pair<Integer, Double> before, int pointIndex, Pair<Integer, Double> after, int pointIndexNext) {
			double mu2 = (1 - Math.cos(mu * Math.PI)) / 2;
			return (before.value * (1 - mu2) + after.value * mu2);
		}
		
	}
	
	public static abstract class AdvancedValueTimeline extends ValueTimeline {
		
		protected double getValue(int index) {
			if (index < 0)
				return points.getFirst().getValue();
			if (index >= points.size())
				return points.getLast().getValue();
			return points.get(index).getValue();
		}
	}
	
	public static class CubicTimeline extends AdvancedValueTimeline {
		
		public CubicTimeline() {
			
		}
		
		@Override
		public double valueAt(double mu, Pair<Integer, Double> before, int pointIndex, Pair<Integer, Double> after, int pointIndexNext) {
			double v0 = getValue(pointIndex - 1);
			double v1 = getValue(pointIndex);
			double v2 = getValue(pointIndexNext);
			double v3 = getValue(pointIndexNext + 1);
			
			double a0, a1, a2, a3, mu2;
			
			mu2 = mu * mu;
			a0 = v3 - v2 - v0 + v1;
			a1 = v0 - v1 - a0;
			a2 = v2 - v0;
			a3 = v1;
			
			return (a0 * mu * mu2 + a1 * mu2 + a2 * mu + a3);
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
	}
	
	public static class HermiteTimeline extends AdvancedValueTimeline {
		
		public Tension tension;
		public double bias;
		
		public HermiteTimeline() {
			tension = Tension.Normal;
			bias = 0;
		}
		
		@Override
		public double valueAt(double mu, Pair<Integer, Double> before, int pointIndex, Pair<Integer, Double> after, int pointIndexNext) {
			double m0, m1, mu2, mu3;
			double a0, a1, a2, a3;
			
			double v0 = getValue(pointIndex - 1);
			double v1 = getValue(pointIndex);
			double v2 = getValue(pointIndexNext);
			double v3 = getValue(pointIndexNext + 1);
			
			mu2 = mu * mu;
			mu3 = mu2 * mu;
			m0 = (v1 - v0) * (1 + bias) * (1 - tension.value) / 2;
			m0 += (v2 - v1) * (1 - bias) * (1 - tension.value) / 2;
			m1 = (v2 - v1) * (1 + bias) * (1 - tension.value) / 2;
			m1 += (v3 - v2) * (1 - bias) * (1 - tension.value) / 2;
			a0 = 2 * mu3 - 3 * mu2 + 1;
			a1 = mu3 - 2 * mu2 + mu;
			a2 = mu3 - mu2;
			a3 = -2 * mu3 + 3 * mu2;
			
			return (a0 * v1 + a1 * m0 + a2 * m1 + a3 * v2);
		}
		
		@Override
		protected int getAdditionalDataSize() {
			return 3;
		}
		
		@Override
		protected void writeAdditionalData(int[] array, int index) {
			array[index] = tension.ordinal();
			long bits = Double.doubleToLongBits(bias);
			array[index + 1] = (int) (bits >> 32);
			array[index + 2] = (int) bits;
		}
		
		@Override
		protected void readAdditionalData(int[] array, int index) {
			tension = Tension.values()[array[index]];
			bias = Double.longBitsToDouble((((long) array[index + 1]) << 32) | (array[index + 2] & 0xffffffffL));
		}
	}
	
	public void offset(int offset) {
		PairList<Integer, Double> newPoints = new PairList<>();
		for (Pair<Integer, Double> point : points)
			newPoints.add(point.key + offset, point.value);
		this.points = newPoints;
	}
	
}
