package br.com.terminalos.jarvis

import org.junit.Test
import org.junit.Assert.assertTrue

class PlaceholderUnitTest {
    @Test fun projectPackageIsValid() {
        assertTrue("Terminal OS JARVIS".contains("JARVIS"))
    }
}
