/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package org.pegdown.ast;

import org.parboiled.common.ImmutableList;
import org.parboiled.common.StringUtils;

import java.util.List;

/**
 * General node for directives.
 */
public class DirectiveNode extends AbstractNode {

    public enum Format { Inline, LeafBlock, ContainerBlock }

    public final Format format;
    public final String name;
    public final String label;
    public final String source;
    public final DirectiveAttributes attributes;
    public final String contents;
    public final Node contentsNode;

    public DirectiveNode(Format format, String name, String label, String source, DirectiveAttributes attributes, Node labelNode) {
        this(format, name, label, source, attributes, label, labelNode);
    }

    public DirectiveNode(Format format, String name, String label, String source, DirectiveAttributes attributes, String contents, Node contentsNode) {
        this.format = format;
        this.name = name;
        this.label = label;
        this.source = source;
        this.attributes = attributes;
        this.contents = contents;
        this.contentsNode = contentsNode;
    }

    public List<Node> getChildren() {
        return ImmutableList.of(contentsNode);
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(" " + format.name() + " '" + name + "'");
        if (!label.isEmpty()) {
            sb.append(" [" + StringUtils.escape(label) + "]");
        }
        if (!source.isEmpty()) {
            sb.append(" (" + StringUtils.escape(source) + ")");
        }
        if (!attributes.isEmpty()) {
            sb.append(" " + attributes.toString());
        }
        return sb.toString();
    }
}
