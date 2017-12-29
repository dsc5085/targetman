package dc.targetman.ai.graph

import com.badlogic.gdx.ai.pfa.DefaultConnection

class DefaultConnection(fromNode: DefaultNode, toNode: DefaultNode, val type: ConnectionType)
    : DefaultConnection<DefaultNode>(fromNode, toNode)