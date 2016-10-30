package dc.targetman.level;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

public final class PolygonOperations {

	public static void union(final List<Vector2> parentVertices, final List<Vector2> newVertices) {
		for (int i = 0; i < parentVertices.size(); i++) {
			Vector2 parentVertex = parentVertices.get(i);
			int newVertexIndex = newVertices.indexOf(parentVertex);
			if (newVertexIndex >= 0) {
				List<Vector2> addedVertexChain = getVertexChain(parentVertices, newVertices, newVertexIndex + 1);
				parentVertices.addAll(i, addedVertexChain);
				i += addedVertexChain.size();
				removeVertexChain(parentVertices, newVertices, i);
				i--;
			}
		}
	}

	private static void removeVertexChain(final List<Vector2> parentVertices, final List<Vector2> newVertices,
			final int startIndex) {
		for (int i = startIndex; i < parentVertices.size(); i++) {
			if (newVertices.contains(parentVertices.get(i))) {
				parentVertices.remove(i);
				i--;
			} else {
				break;
			}
		}
	}

	private static List<Vector2> getVertexChain(final List<Vector2> parentVertices, final List<Vector2> newVertices,
			final int startIndex) {
		List<Vector2> vertexChain = new ArrayList<Vector2>();
		int i = startIndex;
		boolean adding = true;
		while (adding) {
			if (i >= newVertices.size()) {
				i = 0;
			}
			Vector2 newVertex = newVertices.get(i);
			if (parentVertices.contains(newVertex) || vertexChain.contains(newVertex)) {
				adding = false;
			} else {
				vertexChain.add(newVertex);
				i++;
			}
		}
		return vertexChain;
	}

}
