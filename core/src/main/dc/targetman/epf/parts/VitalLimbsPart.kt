package dc.targetman.epf.parts

import dclib.physics.limb.Limb

class VitalLimbsPart(vararg limbs: Limb) {
	val limbs: List<Limb> = limbs.asList()
}