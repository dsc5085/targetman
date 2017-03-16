package dc.targetman.epf.parts

import dc.targetman.mechanics.weapon.Weapon
import dclib.util.Timer

class InventoryPart(
        val maxNumWeapons: Int,
        val gripperName: String,
        weapon: Weapon
) {
    var tryPickup = false
    val pickupTimer = Timer(0.5f, 0.5f)
    val equippedWeapon get() = weapons.getOrNull(0)
    val isFull get() = weapons.size >= maxNumWeapons && equippedWeapon != null

    private val weapons = mutableListOf<Weapon>()

    init {
        weapons.add(weapon)
    }

    fun pickup(weapon: Weapon) {
        weapons.add(0, weapon)
    }
}