package dc.targetman.mechanics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import dc.targetman.epf.parts.MovementPart
import dc.targetman.mechanics.character.CharacterActions
import dc.targetman.system.InputUtils
import dclib.epf.EntityManager
import dclib.graphics.ScreenHelper
import dclib.system.Updater

class InputUpdater(private val entityManager: EntityManager, private val screenHelper: ScreenHelper): Updater {
    override fun update(delta: Float) {
        processInput()
    }

    private fun processInput() {
        val player = EntityFinder.find(entityManager, Alliance.PLAYER)
        if (player == null) {
            return
        }
        val cursorWorldCoords = InputUtils.getCursorWorldCoord(screenHelper)
        CharacterActions.aim(player, cursorWorldCoords)
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            CharacterActions.moveHorizontal(player, Direction.LEFT)
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            CharacterActions.moveHorizontal(player, Direction.RIGHT)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            CharacterActions.jump(player)
            if (player[MovementPart::class].climbing || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                CharacterActions.climbUp(player)
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            CharacterActions.climbDown(player)
        }
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            CharacterActions.trigger(player)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            CharacterActions.switchWeapon(player)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.X)) {
            CharacterActions.pickup(player)
        }
    }
}