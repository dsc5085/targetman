package dc.targetman.epf.parts;

import dclib.util.FloatRange;
import dclib.util.Timer;

public final class ScalePart {

	private final FloatRange scaleXRange;
	private final Timer scaleTimer;

	public ScalePart(final FloatRange scaleXRange, final float scaleTime) {
		this.scaleXRange = scaleXRange;
		scaleTimer = new Timer(scaleTime);
	}

	public final float getScaleX() {
		return scaleXRange.interpolate(scaleTimer.getElapsedPercent());
	}

	public final Timer getScaleTimer() {
		return scaleTimer;
	}

}
