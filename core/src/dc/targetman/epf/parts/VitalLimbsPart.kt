package dc.targetman.epf.parts

import java.util.ArrayList
import java.util.Arrays
import dclib.physics.limb.Limb

class VitalLimbsPart(vararg limbs: Limb) {
	val limbs: List<Limb> = limbs.asList()
}