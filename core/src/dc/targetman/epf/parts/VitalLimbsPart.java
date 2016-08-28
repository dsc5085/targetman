package dc.targetman.epf.parts;

import dclib.limb.Limb;

public final class VitalLimbsPart {

	private final Limb[] vitalLimbs;

	public VitalLimbsPart(final Limb... vitalLimbs) {
		this.vitalLimbs = vitalLimbs;
	}

	public final Limb[] getVitalLimbs() {
		return vitalLimbs.clone();
	}

}
