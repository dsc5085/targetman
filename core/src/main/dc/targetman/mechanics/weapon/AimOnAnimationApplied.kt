package dc.targetman.mechanics.weapon

import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.epf.parts.WeaponPart
import dc.targetman.skeleton.AnimationAppliedEvent
import dc.targetman.skeleton.SkeletonUtils

class AimOnAnimationApplied() : (AnimationAppliedEvent) -> Unit {
    override fun invoke(event: AnimationAppliedEvent) {
        val weaponPart = event.entity[WeaponPart::class]
        val skeletonPart = event.entity[SkeletonPart::class]
        val skeleton = skeletonPart.skeleton
        val muzzleBone = skeleton.findBone(weaponPart.muzzleName)
        val muzzleBoneRotation = SkeletonUtils.getScaledRotation(muzzleBone.worldRotationX, skeletonPart.baseScale)
        val rotationOffset = weaponPart.aimRotation - muzzleBoneRotation
        val rotatorBone = skeleton.findBone(weaponPart.rotatorName)
        rotatorBone.rotation += rotationOffset
        skeleton.updateWorldTransform()
    }
}