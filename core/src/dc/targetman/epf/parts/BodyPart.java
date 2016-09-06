package dc.targetman.epf.parts;

import com.badlogic.gdx.physics.box2d.Body;

public final class BodyPart {

	private final Body body;

	public BodyPart(final Body body) {
		this.body = body;
	}

	public final Body getBody() {
		return body;
	}

}
