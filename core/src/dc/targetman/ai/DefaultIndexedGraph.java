package dc.targetman.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultConnection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;

import dclib.util.CollectionUtils;

public final class DefaultIndexedGraph implements IndexedGraph<DefaultNode> {

	private final List<DefaultNode> nodes;
	private final Map<DefaultNode, Array<Connection<DefaultNode>>> nodeConnections
	= new HashMap<DefaultNode, Array<Connection<DefaultNode>>>();

	public DefaultIndexedGraph(final List<DefaultNode> nodes) {
		this.nodes = nodes;
		setupConnections();
	}

	@Override
	public final Array<Connection<DefaultNode>> getConnections(final DefaultNode fromNode) {
		return CollectionUtils.get(nodeConnections, fromNode, new Array<Connection<DefaultNode>>());
	}

	@Override
	public final int getNodeCount() {
		return nodes.size();
	}

	@Override
	public final int getIndex(final DefaultNode node) {
		return nodes.indexOf(node);
	}

	public final List<DefaultNode> getNodes() {
		return nodes;
	}

	private void setupConnections() {
		for (int i = 0; i < nodes.size() - 1; i++) {
			DefaultNode node1 = nodes.get(i);
			for (int j = i + 1; j < nodes.size(); j++) {
				DefaultNode node2 = nodes.get(j);
				connect(node1, node2);
				connect(node2, node1);
			}
		}
	}

	private void connect(final DefaultNode node1, final DefaultNode node2) {
		if (node1.canJumpTo(node2.x(), node2.right(), node2.top())) {
			Connection<DefaultNode> connection = new DefaultConnection<DefaultNode>(node1, node2);
			getConnections(node1).add(connection);
		}
	}

}
