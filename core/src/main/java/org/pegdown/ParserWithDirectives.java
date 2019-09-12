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

package org.pegdown;

import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.annotations.Cached;
import org.parboiled.support.StringBuilderVar;
import org.parboiled.support.Var;
import org.pegdown.ast.*;
import org.pegdown.plugins.PegDownPlugins;

import java.util.List;

/**
 * Markdown parser that includes general markdown directives.
 *
 * Based on http://talk.commonmark.org/t/generic-directives-plugins-syntax
 */
public class ParserWithDirectives extends Parser {

    public static char DIRECTIVE_MARKER = '@';
    public static char VAR_SUBSTITUTION_MARKER = '$';

    public ParserWithDirectives(Integer options, Long maxParsingTimeInMillis, Parser.ParseRunnerProvider parseRunnerProvider, PegDownPlugins plugins) {
        super(options, maxParsingTimeInMillis, parseRunnerProvider, plugins);
    }

    // Add directive rules into parser
    // Added like this, rather than plugins, for recursive parsing

    @Override
    public Rule NonLinkInline() {
        Rule nonLinkInline = super.NonLinkInline();
        return FirstOf(EscapedVarSubstitutionStart(), VarSubstitution(), InlineDirective(), nonLinkInline);
    }

    @Override
    public Rule Block() {
        Rule block = super.Block();
        return Sequence(
            ZeroOrMore(BlankLine()),
            FirstOf(BlockDirective(), block)
        );
    }

    // var substitution

    public Rule EscapedVarSubstitutionStart() {
        return Sequence('\\', VAR_SUBSTITUTION_MARKER, push(new SpecialTextNode(match())));
    }

    public Rule VarSubstitution() {
        return Sequence(
                VAR_SUBSTITUTION_MARKER,
                VarName(),
                VAR_SUBSTITUTION_MARKER,
                push(new DirectiveNode(DirectiveNode.Format.Inline, "var", popAsString(),
                        DirectiveNode.Source.Empty, new DirectiveAttributes.AttributeMap(), new SuperNode()))
        );
    }

    public Rule VarName() {
        StringBuilderVar name = new StringBuilderVar();
        return Sequence(
                name.clearContents(),
                Letter(), name.append(matchedChar()),
                ZeroOrMore(
                        FirstOf(
                                Sequence(FirstOf(Alphanumeric(), AnyOf("+-_.")), name.append(matchedChar())),
                                Sequence('\\', VAR_SUBSTITUTION_MARKER, name.append(VAR_SUBSTITUTION_MARKER))
                        )
                ),
                push(name.getString())
        );
    }

    // Inline directive

    public Rule InlineDirective() {
        return NodeSequence(
            DIRECTIVE_MARKER,
            DirectiveName(),
            DirectiveLabel(),
            DirectiveSource(),
            MaybeDirectiveAttributes(),
            push(inlineDirectiveNode())
        );
    }

    public Node inlineDirectiveNode() {
        DirectiveAttributes attributes = (DirectiveAttributes) pop();
        DirectiveNode.Source source = (DirectiveNode.Source) pop();
        Node labelChild = popAsNode();
        String label = extractLabelText(labelChild);
        String name = popAsString();
        return new DirectiveNode(DirectiveNode.Format.Inline, name, label, source, attributes, labelChild);
    }

    public String extractLabelText(Node node) {
        return getContext().getInputBuffer().extract(node.getStartIndex() + 1, node.getEndIndex() - 1);
    }

    // Block directives

    public Rule BlockDirective() {
        return FirstOf(LeafBlockDirective(), ContainerBlockDirective());
    }

    // Leaf block directive

    public Rule LeafBlockDirective() {
        return NodeSequence(
            DIRECTIVE_MARKER, DIRECTIVE_MARKER, Sp(),
            DirectiveName(),
            MaybeDirectiveLabel(),
            DirectiveSource(),
            MaybeDirectiveAttributes(), Sp(), Newline(),
            push(leafBlockDirectiveNode())
        );
    }

    public Node leafBlockDirectiveNode() {
        DirectiveAttributes attributes = (DirectiveAttributes) pop();
        DirectiveNode.Source source = (DirectiveNode.Source) pop();
        Node labelChild = popAsNode();
        String label = extractLabelText(labelChild);
        String name = popAsString();
        return new DirectiveNode(DirectiveNode.Format.LeafBlock, name, label, source, attributes, labelChild);
    }

    // Container block directive

    public Rule ContainerBlockDirective() {
        Var<Integer> markerLength = new Var<Integer>();
        return NodeSequence(
            ContainerBlockDirectiveStart(markerLength),
            ContainerBlockDirectiveContents(markerLength),
            ContainerBlockDirectiveEnd(markerLength),
            push(containerBlockDirectiveNode())
        );
    }

    public Rule ContainerBlockDirectiveStart(Var<Integer> markerLength) {
        return Sequence(
            markerLength.clear(),
            ContainerBlockMarker(markerLength), Sp(),
            DirectiveName(),
            MaybeDirectiveTextLabel(),
            DirectiveSource(),
            MaybeDirectiveAttributes(), Sp(), Newline()
        );
    }

    public Rule ContainerBlockMarker(Var<Integer> markerLength) {
        return Sequence(
            NOrMore(DIRECTIVE_MARKER, 3),
            (markerLength.isSet() && matchLength() == markerLength.get()) ||
              (markerLength.isNotSet() && markerLength.set(matchLength()))
        );
    }

    public Rule ContainerBlockDirectiveContents(Var<Integer> markerLength) {
        StringBuilderVar contents = new StringBuilderVar();
        return Sequence(
            push(getContext().getCurrentIndex()),
            contents.clearContents(),
            OneOrMore(TestNot(ContainerBlockDirectiveEnd(markerLength)), ANY, contents.append(matchedChar())),
            push(parseBlockContents(markerLength, contents.appended("\n\n")))
        );
    }

    public Node parseBlockContents(Var<Integer> markerLength, StringBuilderVar block) {
        // recursively parse inner contents
        Context<Object> context = getContext();
        Integer length = markerLength.get();
        String contents = block.getString();
        Node innerRoot = parseInternal(contents.toCharArray());
        markerLength.set(length);
        setContext(context);
        Integer startIndex = (Integer) pop();
        push(contents);
        return shiftBlockIndices(innerRoot, startIndex);
    }

    public Node shiftBlockIndices(Node node, int delta) {
        ((AbstractNode) node).shiftIndices(delta);
        for (Node subNode : node.getChildren()) {
            shiftBlockIndices(subNode, delta);
        }
        return node;
    }

    public Rule ContainerBlockDirectiveEnd(Var<Integer> markerLength) {
        return Sequence(Newline(), ContainerBlockMarker(markerLength), Sp(), Newline());
    }

    public Node containerBlockDirectiveNode() {
        Node parsedContents = popAsNode();
        String rawContents = popAsString();
        DirectiveAttributes attributes = (DirectiveAttributes) pop();
        DirectiveNode.Source source = (DirectiveNode.Source) pop();
        String label = popAsString();
        String name = popAsString();
        return new DirectiveNode(DirectiveNode.Format.ContainerBlock, name, label, source, attributes, rawContents, parsedContents);
    }

    // General directive elements

    public Rule DirectiveName() {
        return Sequence(DirectiveIdentifier(), push(match()));
    }

    public Rule DirectiveIdentifier() {
        return Sequence(Letter(), ZeroOrMore(FirstOf(Alphanumeric(), AnyOf("-_.:"))));
    }

    public Rule MaybeDirectiveLabel() {
        return NodeSequence(
            FirstOf(Sequence(Sp(), DirectiveLabel()), push(new SuperNode()))
        );
    }

    public Rule DirectiveLabel() {
        return NodeSequence(Label());
    }

    public Rule MaybeDirectiveTextLabel() {
        return FirstOf(Sequence(Sp(), DirectiveTextLabel()), push(""));
    }

    public Rule DirectiveTextLabel() {
        return Enclosed('[', ANY, ']');
    }

    public Rule DirectiveSource() {
        return FirstOf(DirectDirectiveSource(), RefDirectiveSource(), EmptyDirectiveSource());
    }

    public Rule DirectDirectiveSource() {
        return Sequence(
                Sp(),
                Enclosed('(', DirectiveLitChar(), ')'),
                push(new DirectiveNode.Source.Direct(popAsString().replaceAll("\\\\(.)", "$1")))
        );
    }

    public Rule RefDirectiveSource() {
        return Sequence(Sp(), TestNot("[]"), DirectiveTextLabel(), push(new DirectiveNode.Source.Ref(popAsString())));
    }

    public Rule EmptyDirectiveSource() {
        return Sequence(Optional(Sequence(Sp(), "[]")), push(DirectiveNode.Source.Empty));
    }

    public Rule Enclosed(char start, Rule matching, char end) {
        return Sequence(
            start,
            Sequence(ZeroOrMore(TestNot(end), NotNewline(), matching), push(match())),
            end
        );
    }

    public Rule MaybeDirectiveAttributes() {
        return FirstOf(Sequence(Sp(), DirectiveAttributes()), push(new DirectiveAttributes.AttributeMap()));
    }

    public Rule DirectiveAttributes() {
        DirectiveAttributes.Var attributesVar = new DirectiveAttributes.Var();
        return Sequence(
            '{',
            Spn1(),
            ZeroOrMore(DirectiveAttribute(), Spn1(), attributesVar.add(popAsString(), popAsString())),
            '}',
            push(attributesVar.getAttributes()),
            attributesVar.clear()
        );
    }

    public Rule DirectiveAttribute() {
        return FirstOf(
            DirectiveIdentifierAttribute(),
            DirectiveClassAttribute(),
            DirectiveKeyValueAttribute()
        );
    }

    public Rule DirectiveIdentifierAttribute() {
        return Sequence(push("identifier"), '#', DirectiveKey());
    }

    public Rule DirectiveClassAttribute() {
        return Sequence(push("class"), '.', DirectiveKey());
    }

    public Rule DirectiveKeyValueAttribute() {
        return Sequence(DirectiveKey(), '=', DirectiveValue());
    }

    public Rule DirectiveKey() {
        return Sequence(DirectiveIdentifier(), push(match()));
    }

    public Rule DirectiveValue() {
        return FirstOf(
            DirectiveQuoted(),
            Sequence(OneOrMore(Nonspacechar()), push(match()))
        );
    }

    public Rule DirectiveQuoted() {
        return FirstOf(
            Enclosed('"', DirectiveLitChar(), '"'),
            Enclosed('\'', DirectiveLitChar(), '\'')
        );
    }

    public Rule DirectiveLitChar() {
        return FirstOf(DirectiveEscapedChar(), ANY);
    }

    public Rule DirectiveEscapedChar() {
        return Sequence('\\', AnyOf("*_`&[]<>!#\\'\".+-(){}:|~"));
    }

    // Header anchor links with all children nodes
    // The built-in version only includes a single text node

    @Override
    public boolean wrapInAnchor() {
        if (ext(ANCHORLINKS)) {
            SuperNode node = (SuperNode) peek();
            List<Node> children = node.getChildren();
            if (!children.isEmpty()) {
                int startIndex = children.get(0).getStartIndex();
                int endIndex = children.get(children.size() - 1).getEndIndex();
                SuperNode parent = new SuperNode(children);
                parent.setStartIndex(startIndex);
                parent.setEndIndex(endIndex);
                AnchorLinkSuperNode anchor = new AnchorLinkSuperNode(parent);
                anchor.setStartIndex(startIndex);
                anchor.setEndIndex(endIndex);
                children.clear();
                children.add(anchor);
            }
        }
        return true;
    }

}
