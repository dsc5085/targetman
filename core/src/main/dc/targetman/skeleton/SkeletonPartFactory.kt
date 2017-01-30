package dc.targetman.skeleton

import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.attachments.RegionAttachment
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.physics.collision.CollisionCategory
import dclib.epf.Entity
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TransformPart
import dclib.geometry.*
import dclib.graphics.TextureCache
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.DefaultTransform
import dclib.physics.Transform

class SkeletonPartFactory(private val textureCache: TextureCache, private val world: World) {
    fun create(skeleton: Skeleton, atlasName: String, size: Vector2): SkeletonPart {
        val skeletonCopy = Skeleton(skeleton)
        val baseScale = size.div(skeleton.bounds.size).scl(skeleton.rootBone.scaleX, skeleton.rootBone.scaleY)
        val root = createLimb(skeletonCopy.rootBone, baseScale, atlasName)
        return SkeletonPart(skeletonCopy, root)
    }

    private fun createLimb(bone: Bone, baseScale: Vector2, atlasName: String): Limb {
        val regionAttachment = getRegionAttachments(bone).firstOrNull()
        val entity = createLimbEntity(regionAttachment, baseScale, atlasName)
        val limb = Limb(bone, entity)
        for (childBone in bone.children) {
            limb.addChild(createLimb(childBone, baseScale, atlasName))
        }
        return limb
    }

    private fun getRegionAttachments(bone: Bone): List<RegionAttachment> {
        val boneSlots = bone.skeleton.slots.filter { it.bone == bone }
        return boneSlots.map { it.attachment }.filterIsInstance<RegionAttachment>()
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
            val scale = VectorUtils.sign(regionScale)
            return createLimbEntity(size, scale, regionName)
        } else {
            return createPointEntity(baseScale)
        }
    }

    private fun createLimbEntity(
            size: Vector2,
            scale: Vector2,
            regionName: String
    ): Entity {
        val entity = Entity()
        val region = textureCache.getPolygonRegion(regionName)
        val vertices = PolygonUtils.createRectangleVertices(size.x, size.y)
        val transform = createLimbTransform(vertices)
        transform.scale = scale
        entity.attach(TransformPart(transform), SpritePart(region))
        return entity
    }

    private fun createLimbTransform(vertices: FloatArray): Transform {
        val body = Box2dUtils.createDynamicBody(world, vertices, true)
        body.gravityScale = 0f
        Box2dUtils.setFilter(body, CollisionCategory.ALL)
        return Box2dTransform(body)
    }

    private fun createPointEntity(scale: Vector2): Entity {
        val entity = Entity()
        // TODO: Is there a better solution for the comment below?
        // The width and height are fairly arbitrary, but the limb should be large enough such that its geometry
        // contains the bone positions of its children.  Meeting this constraint ensures things work correctly such as
        // Box2D joint connections.
        val polygon = Polygon(PolygonUtils.createRectangleVertices(0.1f, 0.1f))
        val transform = DefaultTransform(polygon, 0f)
        transform.scale = scale
        entity.attach(TransformPart(transform))
        return entity
    }
}