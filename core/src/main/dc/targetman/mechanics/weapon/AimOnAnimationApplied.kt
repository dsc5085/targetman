package dc.targetman.mechanics.weapon

import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.epf.parts.WeaponPart
import dc.targetman.skeleton.AnimationAppliedEvent
import dclib.geometry.VectorUtils

class AimOnAnimationApplied : (AnimationAppliedEvent) -> Unit {
    override fun invoke(event: AnimationAppliedEvent) {
        val weaponPart = event.entity[WeaponPart::class]
        val skeletonPart = event.entity[SkeletonPart::class]
        val muzzle = skeletonPart[weaponPart.muzzleName]
        val muzzleBoneRotation = VectorUtils.getScaledRotation(muzzle.bone.worldRotationX, muzzle.scale)
        val rotationOffset = weaponPart.aimRotation - muzzleBoneRotation
        val rotatorBone = skeletonPart[weaponPart.rotatorName].bone
        rotatorBone.rotation += rotationOffset
        skeletonPart.skeleton.updateWorldTransform()
    }
}