package dc.targetman.skeleton

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.attachments.RegionAttachment
import dc.targetman.physics.collision.CollisionCategory
import dclib.epf.Entity
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TransformPart
import dclib.geometry.PolygonUtils
import dclib.geometry.VectorUtils
import dclib.geometry.abs
import dclib.graphics.TextureUtils
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.DefaultTransform
import dclib.physics.Transform

class LimbFactory(private val world: World) {
    fun create(skeleton: Skeleton, rootScale: Vector2): LimbLink {
        val skeletonCopy = Skeleton(skeleton)
        val root = createLimbLink(skeletonCopy.rootBone, rootScale)
        root.scale.set(rootScale)
        return root
    }

    fun link(childSkeleton: Skeleton, rootScale: Vector2, parentLimb: Limb): LimbLink {
        val rootLimb = create(childSkeleton, rootScale)
        val root = LimbLink(rootLimb.limb, LinkType.WEAK, rootScale)
        parentLimb.append(root)
        return root
    }

    private fun createLimbLink(bone: Bone, rootScale: Vector2): LimbLink {
        val regionAttachment = getRegionAttachments(bone).firstOrNull()
        val limbEntity = createLimbEntity(regionAttachment, rootScale)
        val limb = Limb(bone, limbEntity)
        for (childBone in bone.children) {
            limb.append(createLimbLink(childBone, rootScale))
        }
        return LimbLink(limb, LinkType.STRONG)
    }

    private fun getRegionAttachments(bone: Bone): List<RegionAttachment> {
        val boneSlots = bone.skeleton.slots.filter { it.bone == bone }
        return SkeletonUtils.getRegionAttachments(boneSlots)
    }

    private fun createLimbEntity(regionAttachment: RegionAttachment?, rootScale: Vector2): Entity {
        if (regionAttachment != null) {
            val regionScale = Vector2(regionAttachment.scaleX, regionAttachment.scaleY)
            val size = Vector2(regionAttachment.width, regionAttachment.height).scl(rootScale).scl(regionScale.abs())
            val flipScale = VectorUtils.sign(regionScale)
            return createBoneEntity(size, flipScale, regionAttachment.region)
        } else {
            return createPointEntity()
        }
    }

    private fun createBoneEntity(size: Vector2, flipScale: Vector2, textureRegion: TextureRegion): Entity {
        val region = TextureUtils.createPolygonRegion(textureRegion)
        val vertices = PolygonUtils.createRectangleVertices(size.x, size.y)
        val transform = createLimbTransform(vertices)
        transform.setScale(flipScale)
        return Entity(TransformPart(transform), SpritePart(region))
    }

    private fun createLimbTransform(vertices: FloatArray): Transform {
        val body = Box2dUtils.createDynamicBody(world, vertices, true)
        Box2dUtils.setFilter(body, CollisionCategory.ALL)
        return Box2dTransform(body)
    }

    private fun createPointEntity(): Entity {
        val polygon = Polygon(PolygonUtils.createRectangleVertices(0.1f, 0.1f))
        val transform = DefaultTransform(polygon, 0f)
        return Entity(TransformPart(transform))
    }
}