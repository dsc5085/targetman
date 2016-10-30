package dc.targetman.level;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

public final class PolygonOperations {

	public static void union(final List<Vector2> parentVertices, final List<Vector2> newVertices) {
		for (int parentIndex = 0; parentIndex < parentVertices.size() - 1; parentIndex++) {
			int newVertexIndex = newVertices.indexOf(parentVertices.get(parentIndex));
			if (newVertexIndex >= 0) {
				List<Vector2> vertexChainToAdd = getVertexChain(parentVertices, newVertices, newVertexIndex + 1);
				parentVertices.addAll(parentIndex, vertexChainToAdd);
				parentIndex += vertexChainToAdd.size();
				removeVertexChain(parentVertices, newVertices, parentIndex);
				parentIndex--;
			}
		}
	}

	private static List<Vector2> getVertexChain(final List<Vector2> parentVertices,
			final List<Vector2> newVertices, final int start) {
		List<Vector2> vertexChain = new ArrayList<Vector2>();
		int newVertexIndex = start;
		boolean adding = true;
		while (adding) {
			newVertexIndex = wrapIndex(newVertexIndex, newVertices);
			Vector2 newVertex = newVertices.get(newVertexIndex);
			if (parentVertices.contains(newVertex) || vertexChain.contains(newVertex)) {
				adding = false;
			} else {
				vertexChain.add(newVertex);
				newVertexIndex++;
			}
		}
		return vertexChain;
	}

	private static void removeVertexChain(final List<Vector2> parentVertices, final List<Vector2> newVertices,
			final int start) {
		while (start < parentVertices.size() && newVertices.contains(parentVertices.get(start))) {
			parentVertices.remove(start);
		}
	}

	private static int wrapIndex(final int index, final List<Vector2> vertices) {
		int wrappedIndex = index;
		if (index < 0) {
			wrappedIndex = vertices.size() - 1;
		} else if (index >= vertices.size()) {
			wrappedIndex = 0;
		}
		return wrappedIndex;
	}

}
