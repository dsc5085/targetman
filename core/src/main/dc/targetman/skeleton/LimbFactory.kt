package dc.targetman.skeleton

import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.Slot
import com.esotericsoftware.spine.attachments.RegionAttachment
import dc.targetman.physics.collision.CollisionCategory
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TransformPart
import dclib.geometry.*
import dclib.graphics.TextureCache
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.DefaultTransform
import dclib.physics.Transform

// TODO: probably don't need textureCache since we can just reuse existing texture
class LimbFactory(
        private val entityManager: EntityManager,
        private val world: World,
        private val textureCache: TextureCache
) {
    fun create(skeleton: Skeleton, atlasName: String, size: Vector2): Limb {
        val baseScale = getBaseScale(skeleton, size)
        val skeletonCopy = Skeleton(skeleton)
        return createLimb(skeletonCopy.rootBone, baseScale, atlasName)
    }

    fun append(childSkeleton: Skeleton, atlasName: String, size: Vector2, parentLimb: Limb) {
        val baseScale = getBaseScale(childSkeleton, size)
        val skeletonCopy = Skeleton(childSkeleton)
        val duplicateChildLimb = parentLimb.getChildren(true)
                .firstOrNull { it.name == skeletonCopy.rootBone.data.name }
        if (duplicateChildLimb != null) {
            remove(parentLimb, duplicateChildLimb)
        }
        val newBone = append(parentLimb.bone, skeletonCopy.rootBone)
        parentLimb.skeleton.updateCache()
        parentLimb.addChild(createLimb(newBone, baseScale, atlasName))
    }

    private fun getBaseScale(skeleton: Skeleton, size: Vector2): Vector2 {
        return size.div(skeleton.bounds.size).scl(skeleton.rootBone.scaleX, skeleton.rootBone.scaleY)
    }

    private fun remove(parentLimb: Limb, limb: Limb) {
        parentLimb.removeChild(limb)
        parentLimb.bone.children.removeValue(limb.bone, true)
        remove(limb)
    }

    private fun remove(limb: Limb) {
        val bone = limb.bone
        for (child in limb.getChildren(true)) {
            remove(child)
        }
        val skeleton = bone.skeleton
        skeleton.slots.removeAll { it.bone === bone }
        skeleton.drawOrder.removeAll { it.bone === bone }
        skeleton.bones.removeValue(bone, true)
        entityManager.remove(limb.entity)
    }

    private fun append(parentBone: Bone, bone: Bone): Bone {
        val parentSkeleton = parentBone.skeleton
        val newChildBone = Bone(bone.data, parentSkeleton, parentBone)
        parentSkeleton.bones.add(newChildBone)
        for (slot in bone.skeleton.slots.filter { it.bone === bone }) {
            val newSlot = Slot(slot, newChildBone)
            parentSkeleton.slots.add(newSlot)
            parentSkeleton.drawOrder.add(newSlot)
        }
        for (child in bone.children) {
            newChildBone.children.add(append(newChildBone, child))
        }
        return newChildBone
    }

    private fun createLimb(bone: Bone, baseScale: Vector2, atlasName: String): Limb {
        val regionAttachment = getRegionAttachments(bone).firstOrNull()
        val entity = createLimbEntity(regionAttachment, baseScale, atlasName)
        val limb = Limb(bone, entity)
        for (childBone in bone.children) {
            limb.addChild(createLimb(childBone, baseScale, atlasName))
        }
        entityManager.add(entity)
        return limb
    }

    private fun getRegionAttachments(bone: Bone): List<RegionAttachment> {
        val boneSlots = bone.skeleton.slots.filter { it.bone == bone }
        return SkeletonUtils.getRegionAttachments(boneSlots)
    }

    private fun createLimbEntity(
            regionAttachment: RegionAttachment?,
            baseScale: Vector2,
            atlasName: String
    ): Entity {
        if (regionAttachment != null) {
            val regionScale = Vector2(regionAttachment.scaleX, regionAttachment.scaleY)
            val size = Vector2(regionAttachment.width, regionAttachment.height).scl(baseScale).scl(regionScale.abs())
            val regionName = "$atlasName/${regionAttachment.path}"
            val flipScale = VectorUtils.sign(regionScale)
            return createBoneEntity(size, flipScale, regionName)
        } else {
            return createPointEntity(baseScale)
        }
    }

    private fun createBoneEntity(
            size: Vector2,
            scale: Vector2,
            regionName: String
    ): Entity {
        val region = textureCache.getPolygonRegion(regionName)
        val vertices = PolygonUtils.createRectangleVertices(size.x, size.y)
        val transform = createLimbTransform(vertices)
        transform.setScale(scale)
        return Entity(TransformPart(transform), SpritePart(region))
    }

    private fun createLimbTransform(vertices: FloatArray): Transform {
        val body = Box2dUtils.createDynamicBody(world, vertices, true)
        body.gravityScale = 0f
        Box2dUtils.setFilter(body, CollisionCategory.ALL)
        return Box2dTransform(body)
    }

    private fun createPointEntity(scale: Vector2): Entity {
        // TODO: Is there a better solution for the comment below?
        // The width and height are fairly arbitrary, but the limb should be large enough such that its geometry
        // contains the bone positions of its children.  Meeting this constraint ensures things work correctly such as
        // Box2D joint connections.
        val polygon = Polygon(PolygonUtils.createRectangleVertices(0.1f, 0.1f))
        val transform = DefaultTransform(polygon, 0f)
        transform.setScale(scale)
        return Entity(TransformPart(transform))
    }
}