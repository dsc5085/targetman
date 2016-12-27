package dc.targetman.skeleton

import com.esotericsoftware.spine.Skeleton
import dclib.epf.Entity

data class AnimationAppliedEvent(val entity: Entity, val skeleton: Skeleton)