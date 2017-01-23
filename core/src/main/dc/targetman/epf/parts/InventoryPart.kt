package dc.targetman.epf.parts

import dc.targetman.mechanics.weapon.Weapon

class InventoryPart(val maxNumWeapons: Int, val weaponToEquip: Weapon) {
    var pickup = false

    val equippedWeapon: Weapon
        get() = weapons[equippedWeaponIndex]

    private val weapons = mutableListOf<Weapon>()
    private var equippedWeaponIndex: Int

    init {
        weapons.add(weaponToEquip)
        equippedWeaponIndex = weapons.indexOf(weaponToEquip)
    }

    fun pickup(weapon: Weapon) {
        val index = weapons.indexOf(equippedWeapon)
        weapons.add(index + 1, weapon)
        if (weapons.size >= maxNumWeapons) {
            weapons.remove(equippedWeapon)
        }
    }
}