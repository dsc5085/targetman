package dc.targetman.level.models;

public enum Alliance {

	PLAYER, ENEMY;
	
	public final Alliance getTarget() {
		return this == Alliance.PLAYER ? Alliance.ENEMY : Alliance.PLAYER;
	}

}
