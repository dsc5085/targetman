package dc.targetman.geometry

import com.badlogic.gdx.math.Vector2
import com.google.common.collect.Iterables
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier
import dclib.geometry.PolygonUtils
import java.util.ArrayList
import java.util.Collections

object PolygonOperations {
    fun simplify(vertices: List<Vector2>, distanceTolerance: Double): List<Vector2> {
        val coordinates = toCoordinates(vertices)
        val polygon = GeometryFactory().createPolygon(coordinates)
        val simplifier = DouglasPeuckerSimplifier(polygon)
        simplifier.setDistanceTolerance(distanceTolerance)
        val resultGeometry = simplifier.resultGeometry
        val hasMinPoints = resultGeometry.coordinates.size > PolygonUtils.NUM_TRIANGLE_VERTICES
        return if (hasMinPoints) toVectors(resultGeometry) else vertices
    }

    fun union(verticesList: List<List<Vector2>>): List<List<Vector2>> {
        val unionVertices = ArrayList<List<Vector2>>()
        val factory = GeometryFactory()
        val geometries = toGeometries(verticesList, factory)
        val geometriesArray = GeometryFactory.toGeometryArray(geometries)
        val union = factory.createGeometryCollection(geometriesArray).union()
        for (i in 0..union.numGeometries - 1) {
            val geometry = union.getGeometryN(i)
            unionVertices.add(toVectors(geometry))
        }
        return unionVertices
    }

    private fun toGeometries(verticesList: List<List<Vector2>>, factory: GeometryFactory): List<Geometry> {
        val geometries = ArrayList<Geometry>()
        for (vertices in verticesList) {
            val polygon = factory.createPolygon(toCoordinates(vertices))
            geometries.add(polygon)
        }
        return geometries
    }

    private fun toCoordinates(vectors: List<Vector2>): Array<Coordinate> {
        val coordinates = ArrayList<Coordinate>()
        for (vector in vectors) {
            coordinates.add(Coordinate(vector.x.toDouble(), vector.y.toDouble()))
        }
        coordinates.add(coordinates[0])
        return Iterables.toArray(coordinates, Coordinate::class.java)
    }

    private fun toVectors(geometry: Geometry): List<Vector2> {
        val vectors = ArrayList<Vector2>()
        for (coordinate in geometry.coordinates) {
            vectors.add(Vector2(coordinate.x.toFloat(), coordinate.y.toFloat()))
        }
        vectors.removeAt(vectors.size - 1)
        Collections.reverse(vectors)
        return vectors
    }
}
