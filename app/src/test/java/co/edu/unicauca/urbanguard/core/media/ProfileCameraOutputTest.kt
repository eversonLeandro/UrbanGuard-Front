package co.edu.unicauca.urbanguard.core.media

import android.net.Uri
import co.edu.unicauca.urbanguard.R
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProfileCameraOutputTest {
    @Test fun `profile cache path is configured and cancelled camera file is removed`() {
        val context = RuntimeEnvironment.getApplication()
        val xml = context.resources.getXml(R.xml.file_paths)
        var foundProfile = false
        while (xml.next() != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
            if (xml.name == "cache-path" && xml.getAttributeValue(null, "name") == "profile") {
                foundProfile = xml.getAttributeValue(null, "path") == "profile/"
            }
        }
        assertTrue(foundProfile)
        val preparer = ProfilePhotoPreparer(context)
        val dir = File(context.cacheDir, "profile").apply { mkdirs() }
        val file = File.createTempFile("capture_", ".jpg", dir)
        assertTrue(file.exists())
        preparer.discardCameraOutput(Uri.parse("content://test/profile/${file.name}"))
        assertFalse(file.exists())
    }
}
