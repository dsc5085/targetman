package dc.targetman.mechanics;

public final class Weapon {

	private final float reloadTime;
	private final int numBullets;
	private final float spread;
	private final String bulletType;

	public Weapon(final float reloadTime, final int numBullets, final float spread, final String bulletType) {
		this.reloadTime = reloadTime;
		this.numBullets = numBullets;
		this.spread = spread;
		this.bulletType = bulletType;
	}

	public final float getReloadTime() {
		return reloadTime;
	}

	public final int getNumBullets() {
		return numBullets;
	}

	public final float getSpread() {
		return spread;
	}

	public final String getBulletType() {
		return bulletType;
	}

}