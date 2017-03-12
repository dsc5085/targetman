package dc.targetman.epf.parts

import dc.targetman.mechanics.weapon.Weapon
import dclib.util.Timer

class InventoryPart(
        val maxNumWeapons: Int,
        val gripperName: String,
        weaponToEquip: Weapon
) {
    var tryPickup = false
    val pickupTimer = Timer(0.5f, 0.5f)

    val equippedWeapon: Weapon
        get() = weapons[equippedWeaponIndex]

    private val weapons = mutableListOf<Weapon>()
    private var equippedWeaponIndex: Int

    init {
        weapons.add(weaponToEquip)
        equippedWeaponIndex = weapons.indexOf(weaponToEquip)
    }

    fun pickup(weapon: Weapon): Weapon? {
        var removedWeapon: Weapon? = null
        val index = weapons.indexOf(equippedWeapon)
        weapons.add(index + 1, weapon)
        if (weapons.size >= maxNumWeapons) {
            removedWeapon = equippedWeapon
            weapons.remove(removedWeapon)
        }
        return removedWeapon
    }
}