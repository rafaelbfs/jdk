/*
 * Copyright (c) 2022, 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug      8298405
 * @summary  Markdown support in the standard doclet
 * @library  /tools/lib ../../lib
 * @modules  jdk.javadoc/jdk.javadoc.internal.tool
 * @build    toolbox.ToolBox javadoc.tester.* DivTaglet SpanTaglet
 * @run main TestMarkdownTaglets
 */

import javadoc.tester.JavadocTester;
import toolbox.ToolBox;

import java.nio.file.Path;
import java.util.List;

public class TestMarkdownTaglets extends JavadocTester {

    public static void main(String... args) throws Exception {
        var tester = new TestMarkdownTaglets();
        tester.runTests();
    }

    ToolBox tb = new ToolBox();

    // The following test checks whether the output of system taglets
    // is or is not wrapped in <p> tags, depending on the context within
    // the doc comment and the output generated by the taglet.
    //
    // {@code ...} is used as an example of a tag that generates phrasing content.
    // {@snippet ...} is used as an example of a tag that generates block content.
    //
    // See also: testUserTaglets
    @Test
    public void testSystemTaglets(Path base) throws Exception {
        Path src = base.resolve("src");
        tb.writeJavaFiles(src,
                """
                    package p;
                    public class C {
                        /// First sentence.
                        ///
                        /// {@code this is code_standalone}
                        ///
                        /// Lorem ipsum.
                        public void code_standalone() { }

                        /// First sentence.
                        ///
                        /// Before.
                        /// {@code this is code_wrapped}
                        /// After.
                        ///
                        /// Lorem ipsum.
                        public void code_wrapped() { }

                        /// First sentence.
                        ///
                        /// {@snippet :
                        ///    this is snippet_standalone
                        /// }
                        ///
                        /// Lorem ipsum.
                        public void snippet_standalone() { }

                        /// First sentence.
                        ///
                        /// Before.
                        /// {@snippet :
                        ///    this is a snippet_wrapped
                        /// }
                        /// After.
                        ///
                        /// Lorem ipsum.
                        public void snippet_wrapped() { }
                    }""");

        javadoc("-d", base.resolve("api").toString(),
                "-Xdoclint:none",
                "--source-path", src.toString(),
                "p");

        checkOutput("p/C.html", true,
                """
                    <div class="block"><p>First sentence.</p>
                    <p><code>this is code_standalone</code></p>
                    <p>Lorem ipsum.</p>
                    </div>""",
                """
                    <div class="block"><p>First sentence.</p>
                    <p>Before.
                    <code>this is code_wrapped</code>
                    After.</p>
                    <p>Lorem ipsum.</p>
                    </div>""",
                """
                    <div class="block"><p>First sentence.</p>
                    <div class="snippet-container"><button class="copy snippet-copy" aria-label="Copy snippet" \
                    onclick="copySnippet(this)"><span data-copied="Copied!">Copy</span>\
                    <img src="../resource-files/copy.svg" alt="Copy snippet"></button>
                    <pre class="snippet" id="snippet-snippet_standalone()1"><code class="language-java">   this is snippet_standalone
                    </code></pre>
                    </div>

                    <p>Lorem ipsum.</p>
                    </div>""",
                """
                    <div class="block"><p>First sentence.</p>
                    <p>Before.</p>
                    <div class="snippet-container"><button class="copy snippet-copy" aria-label="Copy snippet" onclick="copySnippet(this)"><span data-copied="Copied!">Copy</span><img src="../resource-files/copy.svg" alt="Copy snippet"></button>
                    <pre class="snippet" id="snippet-snippet_wrapped()1"><code class="language-java">   this is a snippet_wrapped
                    </code></pre>
                    </div>

                    <p>After.</p>
                    <p>Lorem ipsum.</p>
                    </div>""");
    }

    // The following test checks whether the output of user-defined taglets
    // is or is not wrapped in <p> tags, depending on the context within
    // the doc comment and the output generated by the taglet.
    //
    // {@span ...} is used as an example of a taglet that generates phrasing content.
    // {@div ...} is used as an example of a taglet that generates block content.
    //
    // See also: testSystemTaglets
    @Test
    public void testUserTaglets(Path base) throws Exception {
        Path src = base.resolve("src");
        tb.writeJavaFiles(src,
                """
                    package p;
                    public class C {
                        /// First sentence.
                        ///
                        /// {@span this is phrasing_standalone}
                        ///
                        /// Lorem ipsum.
                        public void phrasing_standalone() { }

                        /// First sentence.
                        ///
                        /// Before.
                        /// {@span this is phrasing_wrapped}
                        /// After.
                        ///
                        /// Lorem ipsum.
                        public void phrasing_wrapped() { }

                        /// First sentence.
                        ///
                        /// {@div this is block_standalone}
                        ///
                        /// Lorem ipsum.
                        public void block_standalone() { }

                        /// First sentence.
                        ///
                        /// Before.
                        /// {@div this is block_wrapped}
                        /// After.
                        ///
                        /// Lorem ipsum.
                        public void block_wrapped() { }
                    }""");

        String testClasses = System.getProperty("test.classes");

        javadoc("-d", base.resolve("api").toString(),
                "-tagletpath", testClasses,
                "-taglet", "DivTaglet",
                "-taglet", "SpanTaglet",
                "-Xdoclint:none",
                "--source-path", src.toString(),
                "p");

        checkOutput("p/C.html", true, """
                    <div class="block"><p>First sentence.</p>
                    <p><span>this is phrasing_standalone</span></p>
                    <p>Lorem ipsum.</p>
                    </div>""",
                """
                    <div class="block"><p>First sentence.</p>
                    <p>Before.
                    <span>this is phrasing_wrapped</span>
                    After.</p>
                    <p>Lorem ipsum.</p>
                    </div>""",
                """
                    <div class="block"><p>First sentence.</p>
                    <div>this is block_standalone</div>
                    <p>Lorem ipsum.</p>
                    </div>""",
                """
                    <div class="block"><p>First sentence.</p>
                    <p>Before.</p>
                    <div>this is block_wrapped</div>
                    <p>After.</p>
                    <p>Lorem ipsum.</p>
                    </div>""");
    }
}