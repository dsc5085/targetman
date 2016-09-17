package dc.targetman.epf.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dclib.physics.limb.Limb;

public final class VitalLimbsPart {

	private final List<Limb> vitalLimbs;

	public VitalLimbsPart(final Limb... vitalLimbs) {
		this.vitalLimbs = Arrays.asList(vitalLimbs);
	}

	public final List<Limb> getVitalLimbs() {
		return new ArrayList<Limb>(vitalLimbs);
	}

}
