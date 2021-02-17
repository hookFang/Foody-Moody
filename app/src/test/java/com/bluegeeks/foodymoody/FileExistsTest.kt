package com.bluegeeks.foodymoody

import org.junit.Test
import java.io.File

class FileExistsTest {
    /**
     * Unit test- checks if google-services file exists
     */
    @Test
    fun file_found() {
        assert(File("google-services.json").exists())
    }
}