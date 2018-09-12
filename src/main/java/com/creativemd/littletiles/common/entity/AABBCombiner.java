package com.creativemd.littletiles.common.entity;

import java.util.List;

import com.creativemd.creativecore.common.utils.math.box.BoxUtils;
import com.creativemd.creativecore.common.utils.math.box.OrientatedBoundingBox;

public class AABBCombiner {

	public final double deviation;
	public final List<OrientatedBoundingBox> boxes;

	private boolean finished = false;
	private int i = -1;
	private int j = -1;

	/**
	 * how many operations should be done before checking for the time span
	 */
	public int timedOperation = 100;
	/**
	 * in nano seconds
	 */
	public long workingTime = 30000;

	public AABBCombiner(List<OrientatedBoundingBox> boxes, double deviation) {
		this.boxes = boxes;
		this.deviation = deviation;
	}

	public boolean hasFinished() {
		return finished;
	}

	public void work() {
		long started = System.nanoTime();

		int operations = 0;

		boolean skipThrough = i != -1;

		if (!skipThrough)
			i = 0;
		while (i < boxes.size()) {
			if (!skipThrough)
				j = 0;
			while (j < boxes.size()) {
				if (!skipThrough) {
					if (i != j) {
						OrientatedBoundingBox box = combineBoxes(boxes.get(i), boxes.get(j), deviation);
						if (box != null) {
							if (i > j) {
								boxes.remove(i);
								boxes.remove(j);

								i--;
							} else {
								boxes.remove(j);
								boxes.remove(i);
							}

							j = 0;

							boxes.add(box);
							continue;
						}
					}

					operations++;
					if (operations <= timedOperation && System.nanoTime() - started >= workingTime)
						return;
					operations = 0;
				}

				skipThrough = false;
				j++;
			}
			i++;
		}
		finished = true;
	}

	public static OrientatedBoundingBox combineBoxes(OrientatedBoundingBox box1, OrientatedBoundingBox box2, double deviation) {
		if (deviation == 0) {
			boolean x = box1.minX == box2.minX && box1.maxX == box2.maxX;
			boolean y = box1.minY == box2.minY && box1.maxY == box2.maxY;
			boolean z = box1.minZ == box2.minZ && box1.maxZ == box2.maxZ;

			if (x && y && z) {
				return box1;
			}
			if (x && y) {
				if (box1.maxZ >= box2.minZ && box1.minZ <= box2.maxZ)
					return new OrientatedBoundingBox(box1.origin, box1.minX, box1.minY, Math.min(box1.minZ, box2.minZ), box1.maxX, box1.maxY, Math.max(box1.maxZ, box2.maxZ));
			}
			if (x && z) {
				if (box1.maxY >= box2.minY && box1.minY <= box2.maxY)
					return new OrientatedBoundingBox(box1.origin, box1.minX, Math.min(box1.minY, box2.minY), box1.minZ, box1.maxX, Math.max(box1.maxY, box2.maxY), box1.maxZ);
			}
			if (y && z) {
				if (box1.maxX >= box2.minX && box1.minX <= box2.maxX)
					return new OrientatedBoundingBox(box1.origin, Math.min(box1.minX, box2.minX), box1.minY, box1.minZ, Math.max(box1.maxX, box2.maxX), box1.maxY, box1.maxZ);
			}
			return null;
		} else {
			boolean x = BoxUtils.equals(box1.minX, box2.minX, deviation) && BoxUtils.equals(box1.maxX, box2.maxX, deviation);
			boolean y = BoxUtils.equals(box1.minY, box2.minY, deviation) && BoxUtils.equals(box1.maxY, box2.maxY, deviation);
			boolean z = BoxUtils.equals(box1.minZ, box2.minZ, deviation) && BoxUtils.equals(box1.maxZ, box2.maxZ, deviation);

			if (x && y && z)
				return sumBox(box1, box2);

			if (x && y && BoxUtils.greaterEquals(box1.maxZ, box2.minZ, deviation) && BoxUtils.greaterEquals(box2.maxZ, box1.minZ, deviation))
				return sumBox(box1, box2);

			if (x && z && BoxUtils.greaterEquals(box1.maxY, box2.minY, deviation) && BoxUtils.greaterEquals(box2.maxY, box1.minY, deviation))
				return sumBox(box1, box2);

			if (y && z && BoxUtils.greaterEquals(box1.maxX, box2.minX, deviation) && BoxUtils.greaterEquals(box2.maxX, box1.minX, deviation))
				return sumBox(box1, box2);

			return null;
		}
	}

	public static OrientatedBoundingBox sumBox(OrientatedBoundingBox box1, OrientatedBoundingBox box2) {
		return new OrientatedBoundingBox(box1.origin, Math.min(box1.minX, box2.minX), Math.min(box1.minY, box2.minY), Math.min(box1.minZ, box2.minZ), Math.max(box1.maxX, box2.maxX), Math.max(box1.maxY, box2.maxY), Math.max(box1.maxZ, box2.maxZ));
	}
}
