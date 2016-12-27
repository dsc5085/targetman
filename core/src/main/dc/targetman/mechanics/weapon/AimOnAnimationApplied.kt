package dc.targetman.mechanics.weapon

import dc.targetman.epf.parts.WeaponPart
import dc.targetman.skeleton.AnimationAppliedEvent

class AimOnAnimationApplied() : (AnimationAppliedEvent) -> Unit {
    override fun invoke(event: AnimationAppliedEvent) {
        val weaponPart = event.entity[WeaponPart::class]
        val skeleton = event.skeleton
        val muzzleBone = skeleton.findBone(weaponPart.muzzleName)
        val rotationOffset = weaponPart.aimRotation - muzzleBone.worldRotationX
        val rotatorBone = skeleton.findBone(weaponPart.rotatorName)
        rotatorBone.rotation += rotationOffset
        skeleton.updateWorldTransform()
    }
}