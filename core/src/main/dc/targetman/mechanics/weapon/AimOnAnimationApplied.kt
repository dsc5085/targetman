package dc.targetman.mechanics.weapon

import dc.targetman.epf.parts.FiringPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.skeleton.AnimationAppliedEvent
import dclib.geometry.VectorUtils

class AimOnAnimationApplied : (AnimationAppliedEvent) -> Unit {
    override fun invoke(event: AnimationAppliedEvent) {
        val firingPart = event.entity[FiringPart::class]
        val skeletonPart = event.entity[SkeletonPart::class]
        val muzzle = skeletonPart[firingPart.muzzleName]
        val muzzleBoneRotation = VectorUtils.getScaledRotation(muzzle.bone.worldRotationX, muzzle.scale)
        val rotationOffset = firingPart.aimRotation - muzzleBoneRotation
        val rotatorBone = skeletonPart[firingPart.rotatorName].bone
        rotatorBone.rotation += rotationOffset
        skeletonPart.skeleton.updateWorldTransform()
    }
}