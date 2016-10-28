package dc.targetman.mechanics;

public enum Direction {

	NONE, LEFT, RIGHT;
	
	public static final Direction from(final float x) {
		if (x == 0) {
			return NONE;
		} else if (x < 0) {
			return LEFT;
		} else {
			return RIGHT;
		}
	}
	
	public final float toFloat() {
		if (this == NONE) {
			return 0;
		} else if (this == LEFT) {
			return -1;
		} else {
			return 1;
		}
	}
	
}
