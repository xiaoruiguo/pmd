/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import static net.sourceforge.pmd.lang.java.ParserTstUtil.parseJava14;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.sourceforge.pmd.PMD;

public class EncodingTest {

    @Test
    public void testDecodingOfUTF8() throws Exception {
        ASTCompilationUnit acu = parseJava14(TEST_UTF8);
        String methodName = acu.getFirstDescendantOfType(ASTMethodDeclaration.class).getImage();
        assertEquals("é", methodName);
    }

    private static final String TEST_UTF8 = "class Foo {" + PMD.EOL + "  void é() {}" + PMD.EOL + "  void fiddle() {}"
            + PMD.EOL + "}";
}
