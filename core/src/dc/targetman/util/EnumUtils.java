package dc.targetman.util;

public final class EnumUtils {

	private EnumUtils() {
	}

	public static final <E extends Enum<E>> int[] toIntArray(final E[] enumValues) {
		int[] ints = new int[enumValues.length];
		for (int i = 0; i < enumValues.length; i++) {
			ints[i] = enumValues[i].ordinal();
		}
		return ints;
	}

}
