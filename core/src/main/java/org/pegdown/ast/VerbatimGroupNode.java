/*
 * Copyright © 2015 - 2017 Lightbend, Inc. <http://www.lightbend.com>
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

public class VerbatimGroupNode extends VerbatimNode {
    private final String group;
    private final List<String> classes;

    public VerbatimGroupNode(String text) {
        this(text, "", "", Collections.<String>emptyList());
    }

    public VerbatimGroupNode(String text, String type) {
    	this(text, type, "", Collections.<String>emptyList());
    }

    public VerbatimGroupNode(String text, String type, String group, List<String> classes) {
    	super(text, type);
    	this.group = group;
      this.classes = classes;
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
}