package dc.targetman.skeleton

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import dclib.epf.Entity
import dclib.epf.parts.TransformPart
import dclib.geometry.VectorUtils
import dclib.geometry.abs
import dclib.physics.Box2dTransform
import dclib.physics.Transform
import dclib.util.FloatRange
import dclib.util.Maths

class RagdollFactory(private val world: World) {
    fun create(limb: Limb): Limb {
        val newTransform = createLimbTransform(limb.transform)
        val entity = Entity(TransformPart(newTransform))
        val newLimb = Limb(limb.bone, entity)
        for (childLimb in limb.getChildren()) {
            val newChildLimb = create(childLimb)
            newLimb.addChild(newChildLimb)
            val newChildTransform = newChildLimb.transform as Box2dTransform
            val jointAnchor = getJointAnchor(limb, childLimb)
            createJoint(newTransform, newChildTransform, childLimb, jointAnchor)
        }
        return newLimb
    }

    private fun getJointAnchor(parentLimb: Limb, childLimb: Limb): Vector2 {
        var anchor = Vector2(childLimb.bone.worldX, childLimb.bone.worldY)
        val parentTransform = parentLimb.transform as? Box2dTransform
        val childTransform = childLimb.transform as? Box2dTransform
        if (parentTransform != null && childTransform != null) {
            val joint = parentTransform.body.jointList.firstOrNull { it.other === childTransform.body }?.joint
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
        val lowerAngle = when (limbName) {
            "head" -> -30f
            "neck" -> -60f
            "torso" -> 15f
            "right_hand" -> -85f
            "right_forearm" -> 0f
            "right_bicep" -> -180f
            "right_foot" -> 45f
            "right_shin" -> -135f
            "right_thigh" -> 200f
            "left_hand" -> -85f
            "left_forearm" -> 0f
            "left_bicep" -> -180f
            "left_foot" -> 45f
            "left_shin" -> -135f
            "left_thigh" -> 200f
            else -> 0f
        }
        val upperAngle = when (limbName) {
            "head" -> 30f
            "neck" -> 15f
            "torso" -> 100f
            "right_hand" -> 85f
            "right_forearm" -> 150f
            "right_bicep" -> 180f
            "right_foot" -> 100f
            "right_shin" -> 0f
            "right_thigh" -> 360f
            "left_hand" -> 85f
            "left_forearm" -> 150f
            "left_bicep" -> 180f
            "left_foot" -> 100f
            "left_shin" -> 0f
            "left_thigh" -> 360f
            else -> 0f
        }
        return FloatRange(lowerAngle, upperAngle)
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

    private fun createLimbTransform(transform: Transform): Box2dTransform {
        val newTransform: Box2dTransform
        if (transform is Box2dTransform) {
            newTransform = Box2dTransform(transform)
        } else {
            newTransform = Box2dTransform(transform, world)
        }
        val body = newTransform.body
        body.gravityScale = 1f
        for (fixture in body.fixtureList) {
            fixture.isSensor = false
        }
        return newTransform
    }
}