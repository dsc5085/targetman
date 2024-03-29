package dc.targetman.physics.collision;

/**
 * Box2d collision categories, each differentiated by a power of 2.
 */
public final class CollisionCategory {

	public static final short ALL = -1;
	public static final short BOUNDS = 0x0001;
	public static final short PROJECTILE = 0x0002;
	public static final short STATIC = 0x0004;

}
