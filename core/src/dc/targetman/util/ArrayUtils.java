package dc.targetman.util;

import com.badlogic.gdx.utils.Array;

public final class ArrayUtils {

	private ArrayUtils() {
	}

	public static <T> Array<T> toArray(final Iterable<T> iterable) {
		Array<T> array = new Array<T>();
		for (T element : iterable) {
			array.add(element);
		}
		return array;
	}

}
