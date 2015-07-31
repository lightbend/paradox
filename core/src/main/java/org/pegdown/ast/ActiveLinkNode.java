/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package org.pegdown.ast;

import org.parboiled.common.ImmutableList;

import java.util.List;

/**
 * An active link is an explicit link with an 'active' class attribute.
 */
public class ActiveLinkNode extends AbstractNode {

    public final String href;
    public final Node child;

    public ActiveLinkNode(String href, Node child) {
        this.href = href;
        this.child = child;
    }

    public List<Node> getChildren() {
        return ImmutableList.of(child);
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
