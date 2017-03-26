package dc.targetman.epf.parts

import dclib.util.FloatRange

/**
 * @param valueRange shadow brightness range between 0 and 1
 * @param keyLimbNames name of limbs where shadowing value intensifies, ordered from foreground to background limbs
 */
data class LimbsShadowingPart(val valueRange: FloatRange, val keyLimbNames: List<String>)