package dc.targetman.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public final class PolygonOperations {

	public static List<List<Vector2>> union(final List<List<Vector2>> verticesList) {
		List<List<Vector2>> unionVertices = new ArrayList<List<Vector2>>();
		GeometryFactory factory = new GeometryFactory();
		List<Geometry> geometries = toGeometries(verticesList, factory);
		Geometry[] geometriesArray = GeometryFactory.toGeometryArray(geometries);
		Geometry union = factory.createGeometryCollection(geometriesArray).union();
		for (int i = 0; i < union.getNumGeometries(); i++) {
			Geometry geometry = union.getGeometryN(i);
			unionVertices.add(toVectors(geometry));
		}
		return unionVertices;
	}

	private static List<Geometry> toGeometries(final List<List<Vector2>> verticesList, GeometryFactory factory) {
		List<Geometry> geometries = new ArrayList<Geometry>();
		for (List<Vector2> vertices : verticesList) {
			Polygon polygon = factory.createPolygon(toCoordinates(vertices));
			geometries.add(polygon);
		}
		return geometries;
	}

	private static Coordinate[] toCoordinates(final List<Vector2> vectors) {
		List<Coordinate> coordinates = new ArrayList<Coordinate>();
		for (Vector2 vector : vectors) {
			coordinates.add(new Coordinate(vector.x, vector.y));
		}
		coordinates.add(coordinates.get(0));
		return Iterables.toArray(coordinates, Coordinate.class);
	}

	private static List<Vector2> toVectors(final Geometry geometry) {
		List<Vector2> vectors = new ArrayList<Vector2>();
		for (Coordinate coordinate : geometry.getCoordinates()) {
			vectors.add(new Vector2((float)coordinate.x, (float)coordinate.y));
		}
		vectors.remove(vectors.size() - 1);
		Collections.reverse(vectors);
		return vectors;
	}

}
