/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
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

    public final String name;
    public final Node contents;

    public AnchorLinkSuperNode(Node contents) {
        this.name = createAnchor(contents.getChildren());
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
