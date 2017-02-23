package dc.targetman.mechanics.weapon

import dc.targetman.epf.parts.FiringPart
import dc.targetman.skeleton.AnimationAppliedEvent

class AimOnAnimationApplied : (AnimationAppliedEvent) -> Unit {
    override fun invoke(event: AnimationAppliedEvent) {
        val firingPart = event.entity.tryGet(FiringPart::class)
        if (firingPart != null) {
            // TODO:
//            val skeletonPart = event.entity[SkeletonPart::class]
//            val muzzle = skeletonPart[firingPart.muzzleName]
//            val muzzleBoneRotation = VectorUtils.getScaledRotation(muzzle.bone.worldRotationX, muzzle.scale)
//            val rotationOffset = firingPart.aimRotation - muzzleBoneRotation
//            val rotatorBone = skeletonPart[firingPart.rotatorName].bone
//            rotatorBone.rotation += rotationOffset
//            skeletonPart.skeleton.updateWorldTransform()
        }
    }
}