package dc.targetman.skeleton

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import dclib.geometry.VectorUtils
import dclib.geometry.abs
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.util.FloatRange
import dclib.util.Maths

object Ragdoller {
    fun ragdoll(rootLimb: Limb) {
        for (descendant in rootLimb.getDescendants(LinkType.STRONG)) {
            val transform = descendant.transform
            if (transform is Box2dTransform) {
                Box2dUtils.setSensor(transform.body, false)
                destroyJoints(transform)
            }
        }
        addJointsToDescendants(rootLimb)
    }

    private fun destroyJoints(transform: Box2dTransform) {
        for (joint in transform.body.jointList) {
            transform.body.world.destroyJoint(joint.joint)
        }
        transform.body.jointList.clear()
    }

    private fun addJointsToDescendants(limb: Limb) {
        for (childLimb in limb.getChildren(LinkType.STRONG)) {
            val childTransform = childLimb.transform
            if (childTransform is Box2dTransform) {
                val anchorCoords = getAnchorCoords(childLimb)
                createJoint(limb.transform as Box2dTransform, childTransform, childLimb, anchorCoords)
            }
            addJointsToDescendants(childLimb)
        }
    }

    private fun getAnchorCoords(childLimb: Limb): Vector2 {
        val regionAttachment = childLimb.getRegionAttachment()!!
        val regionOffsetFromBone = Vector2(regionAttachment.x, regionAttachment.y)
                .rotate(-regionAttachment.rotation)
                .scl(childLimb.spineScale.abs())
        val boneLocal = childLimb.transform.localCenter.sub(regionOffsetFromBone)
        return childLimb.transform.toWorld(boneLocal)
    }

    private fun createJoint(
            parentTransform: Box2dTransform,
            childTransform: Box2dTransform,
            childLimb: Limb,
            anchorCoords: Vector2
    ) {
        val jointDef = RevoluteJointDef()
        jointDef.initialize(parentTransform.body, childTransform.body, anchorCoords)
        jointDef.enableLimit = true
        // Adds a little "friction", preventing joints from swinging around too freely
        jointDef.enableMotor = true
        jointDef.motorSpeed = 0f
        jointDef.maxMotorTorque = 0.1f
        val angleRange = getAngleRange(childLimb.name)
        setJointAngleRange(jointDef, angleRange, childLimb.bone.rotation, childLimb.spineScale)
        parentTransform.body.world.createJoint(jointDef)
    }

    // TODO: Put these values in a file
    private fun getAngleRange(limbName: String): FloatRange {
        return when (limbName) {
            "head" -> FloatRange(-30f, 30f)
            "neck" -> FloatRange(-60f, 15f)
            "torso" -> FloatRange(45f, 100f)
            "right_hand" -> FloatRange(-30f, 30f)
            "right_forearm" -> FloatRange(0f, 120f)
            "right_bicep" -> FloatRange(-120f, 180f)
            "right_foot" -> FloatRange(45f, 100f)
            "right_shin" -> FloatRange(-135f, 0f)
            "right_thigh" -> FloatRange(200f, 300f)
            "left_hand" -> FloatRange(-30f, 30f)
            "left_forearm" -> FloatRange(0f, 120f)
            "left_bicep" -> FloatRange(-120f, 180f)
            "left_foot" -> FloatRange(45f, 100f)
            "left_shin" -> FloatRange(-135f, 0f)
            "left_thigh" -> FloatRange(200f, 300f)
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
}