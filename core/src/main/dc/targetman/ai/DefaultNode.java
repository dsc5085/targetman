package dc.targetman.ai;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultConnection;
import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DefaultNode {

	private final Vector2 position;
	private final Set<DefaultNode> connectedNodes = new HashSet<DefaultNode>();

	public DefaultNode(final float x, final float y) {
		position = new Vector2(x, y);
	}

	public final float x() {
		return position.x;
	}

	public final float y() {
		return position.y;
	}

	public final Vector2 getPosition() {
		return position.cpy();
	}

	public final List<Connection<DefaultNode>> getConnections() {
		List<Connection<DefaultNode>> connections = new ArrayList<Connection<DefaultNode>>();
		for (DefaultNode node : connectedNodes) {
			DefaultConnection<DefaultNode> connection = new DefaultConnection<DefaultNode>(this, node);
			connections.add(connection);
		}
		return connections;
	}

	public final void addConnection(final DefaultNode endNode) {
		connectedNodes.add(endNode);
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(position);
	}

	@Override
	public final boolean equals(final Object o) {
		return o instanceof DefaultNode && ((DefaultNode)o).position.equals(position);
	}

	@Override
	public final String toString() {
		return position.toString();
	}

}
