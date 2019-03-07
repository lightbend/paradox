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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class VerbatimGroupNode extends VerbatimNode {
    private final String group;
    private final List<String> classes;
    private final Optional<String> sourceUrl;

    public VerbatimGroupNode(String text) {
        this(text, "", "", Collections.<String>emptyList(), Optional.<String>empty());
    }

    public VerbatimGroupNode(String text, String type) {
        this(text, type, "", Collections.<String>emptyList(), Optional.<String>empty());
    }

    public VerbatimGroupNode(String text, String type, String group, List<String> classes, Optional<String> sourceUrl) {
        super(text, type);
        this.group = group;
        this.classes = classes;
        this.sourceUrl = sourceUrl;
    }

    @Override
    public void accept(Visitor visitor) {
    	visitor.visit(this);
    }

    public String getGroup() {
    	return group;
    }

    public List<String> getClasses() {
      return classes;
    }

    public Optional<String> getSourceUrl() { return sourceUrl; }
}