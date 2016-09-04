package dc.targetman.epf.parts;

public final class ForcePart {

	private final float force;
	private final Enum<?> collisionGroup;

	public ForcePart(final float force, final Enum<?> collisionGroup) {
		this.force = force;
		this.collisionGroup = collisionGroup;
	}

	public final float getForce() {
		return force;
	}

	public final Enum<?> getCollisionGroup() {
		return collisionGroup;
	}

}
