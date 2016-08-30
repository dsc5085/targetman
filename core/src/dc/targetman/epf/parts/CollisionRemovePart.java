package dc.targetman.epf.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CollisionRemovePart {

	private final List<Enum<?>> collisionGroups;

	public CollisionRemovePart(final Enum<?>... collisionGroups) {
		this.collisionGroups = Arrays.asList(collisionGroups);
	}

	public final List<Enum<?>> getCollisionGroups() {
		return new ArrayList<Enum<?>>(collisionGroups);
	}

}
