package dc.targetman.epf.parts;

public final class CollisionRemovePart {

	private final Enum<?>[] collisionGroups;

	public CollisionRemovePart(final Enum<?>... collisionGroups) {
		this.collisionGroups = collisionGroups;
	}

	public final Enum<?>[] getCollisionGroups() {
		return collisionGroups.clone();
	}

}
