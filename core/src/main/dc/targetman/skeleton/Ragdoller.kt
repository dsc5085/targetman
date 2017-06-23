package dc.targetman.skeleton

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import dclib.geometry.VectorUtils
import dclib.geometry.abs
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.Transform
import dclib.util.FloatRange
import dclib.util.Maths

class Ragdoller {
    fun ragdoll(limb: Limb) {
        ragdoll(limb.transform)
        for (childLimb in limb.getChildren()) {
            ragdoll(childLimb)
            val jointAnchor = getJointAnchor(limb, childLimb)
            createJoint(limb.transform as Box2dTransform, childLimb.transform as Box2dTransform, childLimb, jointAnchor)
        }
    }

    private fun getJointAnchor(parentLimb: Limb, childLimb: Limb): Vector2 {
        var anchor = Vector2(childLimb.bone.worldX, childLimb.bone.worldY)
        val parentBody = Box2dUtils.getBody(parentLimb.entity)
        val childBody = Box2dUtils.getBody(childLimb.entity)
        if (parentBody != null && childBody != null) {
            val joint = parentBody.jointList.firstOrNull { it.other === childBody }?.joint
            if (joint != null) {
                anchor = joint.anchorA
            }
        }
        return anchor
    }

    private fun createJoint(
            parentTransform: Box2dTransform,
            childTransform: Box2dTransform,
            childLimb: Limb,
            anchor: Vector2
    ) {
        val jointDef = RevoluteJointDef()
        jointDef.initialize(parentTransform.body, childTransform.body, anchor)
        jointDef.collideConnected = true
        jointDef.enableLimit = true
        val angleRange = getAngleRange(childLimb.name)
        setJointAngleRange(jointDef, angleRange, childLimb.bone.rotation, childLimb.scale)
        parentTransform.body.world.createJoint(jointDef)
    }

    // TODO: Put these values in a file
    private fun getAngleRange(limbName: String): FloatRange {
        return when (limbName) {
            "head" -> FloatRange(-30f, 30f)
            "neck" -> FloatRange(-60f, 15f)
            "torso" -> FloatRange(15f, 100f)
            "right_hand" -> FloatRange(-85f, 85f)
            "right_forearm" -> FloatRange(0f, 150f)
            "right_bicep" -> FloatRange(-180f, 180f)
            "right_foot" -> FloatRange(45f, 100f)
            "right_shin" -> FloatRange(-135f, 0f)
            "right_thigh" -> FloatRange(200f, 360f)
            "left_hand" -> FloatRange(-85f, 85f)
            "left_forearm" -> FloatRange(0f, 150f)
            "left_bicep" -> FloatRange(-180f, 180f)
            "left_foot" -> FloatRange(45f, 100f)
            "left_shin" -> FloatRange(-135f, 0f)
            "left_thigh" -> FloatRange(200f, 360f)
            else -> FloatRange(0f, 0f)
        }
    }

    private fun setJointAngleRange(
            jointDef: RevoluteJointDef,
            angleRange: FloatRange,
            childLocalRotation: Float,
            scale: Vector2
    ) {
        val relativeLowerAngle = VectorUtils.getScaledRotation(angleRange.min(), scale.abs()) - childLocalRotation
        val relativeUpperAngle = VectorUtils.getScaledRotation(angleRange.max(), scale.abs()) - childLocalRotation
        val roundedDegrees = Maths.round(relativeUpperAngle, Maths.DEGREES_MAX)
        val lowerAngleDeg = relativeLowerAngle - roundedDegrees
        val upperAngleDeg = relativeUpperAngle - roundedDegrees
        // Flip the angle's sign to handle flipped transform scales
        val angleFlipMultiplier = Math.signum(scale.x) * Math.signum(scale.y)
        val jointRange = FloatRange(lowerAngleDeg * angleFlipMultiplier, upperAngleDeg * angleFlipMultiplier)
        jointDef.lowerAngle = jointRange.min() * MathUtils.degRad
        jointDef.upperAngle = jointRange.max() * MathUtils.degRad
    }

    private fun ragdoll(transform: Transform) {
        if (transform is Box2dTransform) {
            transform.body.gravityScale = 1f
            for (fixture in transform.body.fixtureList) {
                fixture.isSensor = false
            }
        }
    }
}