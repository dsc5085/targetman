package dc.targetman.epf.parts

import dc.targetman.mechanics.weapon.Weapon
import dclib.util.Timer

class InventoryPart(
        val maxNumWeapons: Int,
        val gripperName: String,
        weapon: Weapon
) {
    var trySwitchWeapon = true
    val switchTimer = Timer(0.2f, 0.2f)
    var tryPickup = false
    val pickupTimer = Timer(0.5f, 0.5f)
    val equippedWeapon get() = weapons.getOrNull(equippedIndex)
    val isFull get() = weapons.size >= maxNumWeapons && equippedWeapon != null

    private val weapons = mutableListOf<Weapon>()
    private var equippedIndex = 0

    init {
        weapons.add(weapon)
    }

    fun switchWeapon() {
        equippedIndex++
        if (equippedIndex >= weapons.size) {
            equippedIndex = 0
        }
    }

    fun pickup(weapon: Weapon) {
        weapons.add(weapon)
    }

    fun dropEquippedWeapon() {
        weapons.removeAt(equippedIndex)
        if (equippedIndex >= weapons.size) {
            equippedIndex = 0
        }
    }
}