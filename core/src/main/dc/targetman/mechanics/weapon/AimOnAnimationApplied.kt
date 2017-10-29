package dc.targetman.mechanics.weapon

import com.badlogic.gdx.math.Interpolation
import dc.targetman.epf.parts.FiringPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.skeleton.AnimationAppliedEvent
import dc.targetman.skeleton.Limb
import dclib.geometry.VectorUtils
import dclib.util.Maths

class AimOnAnimationApplied : (AnimationAppliedEvent) -> Unit {
    override fun invoke(event: AnimationAppliedEvent) {
        val firingPart = event.entity.tryGet(FiringPart::class)
        if (firingPart != null) {
            val skeletonPart = event.entity[SkeletonPart::class]
            val muzzle = skeletonPart.tryGet(firingPart.muzzleName)
            if (muzzle != null) {
                val aimToTargetDelta = getAimToTargetDelta(muzzle, firingPart)
                updateAimAccelerationTime(firingPart, event.timeDelta, aimToTargetDelta)
                val frameAimDelta = calculateFrameAimDelta(aimToTargetDelta, firingPart, event.timeDelta)
                firingPart.lastAimDelta = frameAimDelta
                val rotator = skeletonPart[firingPart.rotatorName]
                rotateRotator(rotator, frameAimDelta)
            }
        }
    }

    private fun getAimToTargetDelta(muzzle: Limb, firingPart: FiringPart): Float {
        val targetOffset = VectorUtils.offset(muzzle.transform.center, firingPart.targetCoord)
        return Maths.degDelta(targetOffset.angle(), muzzle.transform.rotation) * getAngleSign(muzzle)
    }

    private fun updateAimAccelerationTime(firingPart: FiringPart, timeDelta: Float, aimToTargetDelta: Float) {
        val changedAimDirection = Math.signum(firingPart.lastAimDelta) != Math.signum(aimToTargetDelta)
        if (changedAimDirection) {
            firingPart.aimAccelerationTime = 0f
        }
        firingPart.aimAccelerationTime += timeDelta
    }

    private fun calculateFrameAimDelta(aimToTargetDelta: Float, firingPart: FiringPart, timeDelta: Float): Float {
        val maxAimSpeed = 960f
        val timeToReachMaxAimSpeed = 0.1f
        val aimToTargetRatio = Math.abs(aimToTargetDelta / Maths.HALF_DEGREES_MAX)
        // Aim speed slows down as it reaches the target aim
        val closingAimDelta = Interpolation.exp10Out.apply(0f, maxAimSpeed, aimToTargetRatio)
        val aimAccelerationProgress = firingPart.aimAccelerationTime / timeToReachMaxAimSpeed
        val accleratedAimDelta = Interpolation.exp5In.apply(0f, maxAimSpeed, aimAccelerationProgress)
        return Math.min(closingAimDelta, accleratedAimDelta) * Math.signum(aimToTargetDelta) * timeDelta
    }

    private fun rotateRotator(rotator: Limb, aimDelta: Float) {
        // Note that the skeleton's rotator bone rotation gets reset every frame, so we have to do some funky math here
        val newAngle = rotator.transform.rotation * getAngleSign(rotator) + aimDelta
        val rotatorBoneRotation = VectorUtils.getScaledRotation(rotator.bone.worldRotationX, rotator.flipScale)
        val rotationOffset = newAngle - rotatorBoneRotation
        rotator.bone.rotation += rotationOffset
    }

    private fun getAngleSign(limb: Limb): Float {
        return limb.flipScale.x * limb.flipScale.y
    }
}