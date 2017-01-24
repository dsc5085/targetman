package dc.targetman.epf.parts

import dc.targetman.mechanics.weapon.Weapon

class InventoryPart(val maxNumWeapons: Int, val weaponLimbName: String, weaponToEquip: Weapon) {
    var pickup = false

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