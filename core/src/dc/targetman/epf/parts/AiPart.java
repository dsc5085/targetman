package dc.targetman.epf.parts;

import java.util.ArrayList;
import java.util.List;

import dc.targetman.ai.DefaultNode;
import dclib.util.Timer;

public final class AiPart {

	private static final float THINK_TIME = 0.1f;

	private final Timer thinkTimer = new Timer(THINK_TIME, THINK_TIME);
	private List<DefaultNode> path = new ArrayList<DefaultNode>();

	public final List<DefaultNode> getPath() {
		return path;
	}

	public final void setPath(final List<DefaultNode> path) {
		this.path = path;
	}

	public final boolean think() {
		return thinkTimer.check();
	}

	public final void tick(final float delta) {
		thinkTimer.tick(delta);
	}

}
