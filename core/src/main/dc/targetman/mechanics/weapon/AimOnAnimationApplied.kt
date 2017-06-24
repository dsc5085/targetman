package dc.targetman.mechanics.weapon

import dc.targetman.epf.parts.FiringPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.skeleton.AnimationAppliedEvent
import dclib.geometry.VectorUtils

class AimOnAnimationApplied : (AnimationAppliedEvent) -> Unit {
    override fun invoke(event: AnimationAppliedEvent) {
        val firingPart = event.entity.tryGet(FiringPart::class)
        if (firingPart != null) {
            val skeletonPart = event.entity[SkeletonPart::class]
            val rotator = skeletonPart[firingPart.rotatorName]
            val muzzleBoneRotation = VectorUtils.getScaledRotation(rotator.bone.worldRotationX, rotator.spineScale)
            val rotationOffset = firingPart.aimRotation - muzzleBoneRotation
            rotator.bone.rotation += rotationOffset
            skeletonPart.skeleton.updateWorldTransform()
        }
    }
}