package dc.targetman.character

import dclib.graphics.TextureCache
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import test.dclib.GdxMock
import test.dclib.GdxTestRunner

@RunWith(GdxTestRunner::class)
class CharacterLoaderTest {
    companion object {
        private val textureCache = TextureCache()

        @BeforeClass
        @JvmStatic
        fun oneTimeSetup() {
            GdxMock.mockGl()
            textureCache.loadTexturesIntoAtlas("assets/textures/skins/man", "skins/man")
        }

        @AfterClass
        @JvmStatic
        fun oneTimeTearDown() {
            textureCache.dispose()
        }
    }

    @Test
    fun create_Valid_Success() {
        val loader = CharacterLoader(textureCache)
        val character = loader.create("assets/skeletons/man_original.skel")
        assert(character.limbs.isNotEmpty())
    }
}