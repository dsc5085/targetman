package dc.targetman.skeleton

import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.attachments.RegionAttachment
import dc.targetman.level.FactoryTools
import dc.targetman.physics.collision.CollisionCategory
import dclib.epf.Entity
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TransformPart
import dclib.geometry.PolygonUtils
import dclib.geometry.VectorUtils
import dclib.geometry.abs
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.Transform

// TODO: probably don't need textureCache since we can just reuse existing texture.
class LimbFactory(private val factoryTools: FactoryTools) {
    private val entityManager = factoryTools.entityManager

    fun create(skeleton: Skeleton, atlasName: String, rootScale: Vector2): Limb {
        val skeletonCopy = Skeleton(skeleton)
        return createLimb(skeletonCopy.rootBone, rootScale, atlasName)
    }

    fun link(childSkeleton: Skeleton, atlasName: String, rootScale: Vector2, parentLimb: Limb): SkeletonRoot {
        val rootLimb = create(childSkeleton, atlasName, rootScale)
        val root = SkeletonRoot(rootLimb, rootScale)
        parentLimb.add(root)
        return root
    }

    fun removeChildren(parentLimb: Limb) {
        for (child in parentLimb.getChildren(includeLinked = true)) {
            remove(parentLimb, child)
        }
    }

    private fun remove(parentLimb: Limb, limb: Limb) {
        val container = LimbUtils.findContainer(entityManager.getAll(), limb.entity)
        if (limb.bone === limb.skeleton.rootBone && container != null) {
            entityManager.remove(container)
        }
        parentLimb.removeChild(limb)
        entityManager.remove(limb.entity)
    }

    private fun createLimb(bone: Bone, rootScale: Vector2, atlasName: String): Limb {
        val regionAttachment = getRegionAttachments(bone).firstOrNull()
        val limbEntity = createLimbEntity(regionAttachment, rootScale, atlasName)
        val limb = Limb(bone, limbEntity)
        for (childBone in bone.children) {
            limb.addChild(createLimb(childBone, rootScale, atlasName))
        }
        return limb
    }

    private fun getRegionAttachments(bone: Bone): List<RegionAttachment> {
        val boneSlots = bone.skeleton.slots.filter { it.bone == bone }
        return SkeletonUtils.getRegionAttachments(boneSlots)
    }

    private fun createLimbEntity(
            regionAttachment: RegionAttachment?,
            rootScale: Vector2,
            atlasName: String
    ): Entity {
        if (regionAttachment != null) {
            val regionScale = Vector2(regionAttachment.scaleX, regionAttachment.scaleY)
            val size = Vector2(regionAttachment.width, regionAttachment.height).scl(rootScale).scl(regionScale.abs())
            val regionName = "$atlasName/${regionAttachment.path}"
            val flipScale = VectorUtils.sign(regionScale)
            return createBoneEntity(size, flipScale, regionName)
        } else {
            return createPointEntity()
        }
    }

    private fun createBoneEntity(
            size: Vector2,
            flipScale: Vector2,
            regionName: String
    ): Entity {
        val region = factoryTools.textureCache.getPolygonRegion(regionName)
        val vertices = PolygonUtils.createRectangleVertices(size.x, size.y)
        val transform = createLimbTransform(vertices)
        transform.setScale(flipScale)
        return Entity(TransformPart(transform), SpritePart(region))
    }

    private fun createPointEntity(): Entity {
        // TODO: Is there a better solution for the comment below?
        // The width and height are fairly arbitrary, but the limb should be large enough such that its geometry
        // contains the bone positions of its children.  Meeting this constraint ensures things work correctly such as
        // Box2D joint connections.
        val transform = createLimbTransform(PolygonUtils.createRectangleVertices(0.1f, 0.1f))
        return Entity(TransformPart(transform))
    }

    private fun createLimbTransform(vertices: FloatArray): Transform {
        val body = Box2dUtils.createDynamicBody(factoryTools.world, vertices, true)
        body.gravityScale = 0f
        Box2dUtils.setFilter(body, CollisionCategory.ALL)
        return Box2dTransform(body)
    }
}