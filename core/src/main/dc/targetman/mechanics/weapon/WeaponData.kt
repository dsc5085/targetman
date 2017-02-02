package dc.targetman.mechanics.weapon

data class WeaponData(
        val reloadTime: Float = 1f,
        val numBullets: Int = 1,
        val spread: Float = 0f,
        val minSpeed: Float = 1f,
        val maxSpeed: Float = 1f,
        val recoil: Float = 0f,
        val width: Float = 1f,
        val skeletonPath: String = "",
        val atlasName: String = "",
        var bullet: Bullet = Bullet())