package dc.targetman.skeleton

import com.badlogic.gdx.math.Polygon
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
import dclib.physics.DefaultTransform
import dclib.physics.Transform

// TODO: probably don't need textureCache since we can just reuse existing texture.
class LimbFactory(private val factoryTools: FactoryTools) {
    fun create(skeleton: Skeleton, atlasName: String, rootScale: Vector2): Limb {
        val skeletonCopy = Skeleton(skeleton)
        return createLimb(skeletonCopy.rootBone, rootScale, atlasName)
    }

    fun link(childSkeleton: Skeleton, atlasName: String, rootScale: Vector2, parentLimb: Limb): SkeletonRoot {
        val rootLimb = create(childSkeleton, atlasName, rootScale)
        val root = SkeletonRoot(rootLimb, rootScale)
        parentLimb.append(root)
        return root
    }

    fun removeChildren(parentLimb: Limb) {
        for (child in parentLimb.getChildren(true, true)) {
            remove(parentLimb, child)
        }
    }

    private fun remove(parentLimb: Limb, limb: Limb) {
        val entityManager = factoryTools.entityManager
        val container = LimbUtils.findContainer(entityManager.getAll(), limb.entity)
        if (limb.bone === limb.skeleton.rootBone && container != null) {
            entityManager.remove(container)
        }
        parentLimb.detach(limb)
        entityManager.remove(limb.entity)
    }

    private fun createLimb(bone: Bone, rootScale: Vector2, atlasName: String): Limb {
        val regionAttachment = getRegionAttachments(bone).firstOrNull()
        val limbEntity = createLimbEntity(regionAttachment, rootScale, atlasName)
        val limb = Limb(bone, limbEntity)
        for (childBone in bone.children) {
            limb.append(createLimb(childBone, rootScale, atlasName))
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

    private fun createLimbTransform(vertices: FloatArray): Transform {
        val body = Box2dUtils.createDynamicBody(factoryTools.world, vertices, true)
        Box2dUtils.setFilter(body, CollisionCategory.ALL)
        return Box2dTransform(body)
    }

    private fun createPointEntity(): Entity {
        val polygon = Polygon(PolygonUtils.createRectangleVertices(0.1f, 0.1f))
        val transform = DefaultTransform(polygon, 0f)
        return Entity(TransformPart(transform))
    }
}