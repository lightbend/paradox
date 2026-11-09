/*
 * Copyright © 2015 - 2019 Lightbend, Inc. <http://www.lightbend.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pegdown.ast;

import org.parboiled.common.ImmutableList;

import java.util.List;

/**
 * Anchor link that can contain any markdown nodes as children.
 * The built-in anchor link only supports a single text node,
 * excluding special characters or inlines in anchored headers.
 */
public class AnchorLinkSuperNode extends AbstractNode {

    /**
     * The anchor name derived from the header text alone.
     */
    public final String baseName;

    /**
     * The anchor name to render. Starts out as {@link #baseName}, and is given a
     * numeric suffix when the same header text is repeated on a page
     * (see {@code Index.uniqueHeaderAnchors}).
     */
    public String name;

    public final Node contents;

    public AnchorLinkSuperNode(Node contents) {
        this.baseName = createAnchor(contents.getChildren());
        this.name = baseName;
        this.contents = contents;
    }

    public List<Node> getChildren() {
        return ImmutableList.of(contents);
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    private String createAnchor(List<Node> children) {
        StringBuilder sb = new StringBuilder();
        for (Node node : children) {
            if (node instanceof TextNode) {
                TextNode textNode = (TextNode) node;
                for (char c : textNode.getText().toCharArray()) {
                    if (Character.isLetterOrDigit(c)) {
                        sb.append(Character.toLowerCase(c));
                    } else if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '-') {
                        sb.append('-');
                    }
                }
            }
        }
        return sb.toString();
    }

}
