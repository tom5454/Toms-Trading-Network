package com.tom.trading.util;

import java.util.Arrays;
import java.util.Comparator;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

public enum BlockFaceDirection {
	BOTTOM(Direction.DOWN, 1, 2, -1),
	TOP(Direction.UP, 1, 0, -1),
	FRONT(Direction.SOUTH, 1, 1, 0),
	LEFT(Direction.WEST, 0, 1, 3),
	BACK(Direction.NORTH, 2, 2, 2),
	RIGHT(Direction.EAST, 2, 1, 1),
	;
	private final Direction dir;
	private final int x, y, d;
	private static final BlockFaceDirection[] FACE = Arrays.stream(values()).filter(d -> d.d != -1).sorted(Comparator.comparingInt(d -> d.d)).toArray(BlockFaceDirection[]::new);

	private BlockFaceDirection(Direction dir, int x, int y, int d) {
		this.dir = dir;
		this.x = x;
		this.y = y;
		this.d = d;
	}

	public Direction getHorizontalMapped(Direction blockDir) {
		if(dir.getAxis() == Axis.Y)return dir;
		else return Direction.from2DDataValue(blockDir.get2DDataValue() + dir.get2DDataValue());
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public static BlockFaceDirection getHorizontalFace(Direction facing, Direction side) {
		if(side.getAxis() == Axis.Y)return side == Direction.UP ? TOP : BOTTOM;
		int f = (facing.getOpposite().get2DDataValue() - side.get2DDataValue() + 4) % 4;
		return FACE[f];
	}
}
