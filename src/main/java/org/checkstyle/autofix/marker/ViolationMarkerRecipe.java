///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle-openrewrite-recipes: Automatically fix Checkstyle violations with OpenRewrite.
// Copyright (C) 2025 The Checkstyle OpenRewrite Recipes Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
///////////////////////////////////////////////////////////////////////////////////////////////

package org.checkstyle.autofix.marker;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.checkstyle.autofix.PositionHelper;
import org.checkstyle.autofix.parser.CheckstyleViolation;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

public class ViolationMarkerRecipe extends Recipe {

    private static final String DISPLAY_NAME = "Checkstyle violation marker";
    private static final String DESCRIPTION =
            "Marks AST nodes that correspond to Checkstyle violations.";

    private final List<CheckstyleViolation> violations;

    public ViolationMarkerRecipe(List<CheckstyleViolation> violations) {
        this.violations = new ArrayList<>(violations);
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ViolationMarkerVisitor(violations);
    }

    private static final class ViolationMarkerVisitor extends JavaIsoVisitor<ExecutionContext> {

        private final List<CheckstyleViolation> violations;
        private Path sourcePath;
        private J.CompilationUnit currentCompilationUnit;

        private ViolationMarkerVisitor(List<CheckstyleViolation> violations) {
            this.violations = violations;
        }

        @Override
        public J.CompilationUnit visitCompilationUnit(
                J.CompilationUnit cu, ExecutionContext executionContext) {
            this.sourcePath = cu.getSourcePath();
            this.currentCompilationUnit = cu;
            return super.visitCompilationUnit(cu, executionContext);
        }

        @Override
        public J.ClassDeclaration visitClassDeclaration(
                J.ClassDeclaration classDeclaration, ExecutionContext executionContext) {

            J.ClassDeclaration result =
                    super.visitClassDeclaration(classDeclaration, executionContext);

            if (result.getMarkers()
                    .findAll(CheckstyleViolationMarker.class).isEmpty()) {

                final int line = PositionHelper.computeLinePosition(
                        currentCompilationUnit, classDeclaration, getCursor());

                final List<CheckstyleViolation> matchedViolations = violations.stream()
                        .filter(violation -> {
                            return violation.getLine() == line
                                    && violation.getFilePath().endsWith(sourcePath);
                        })
                        .toList();

                for (CheckstyleViolation violation : matchedViolations) {
                    CheckstyleViolationMarker marker =
                            new CheckstyleViolationMarker(Tree.randomId(), violation);
                    marker = marker.withId(marker.getId());

                    result = result.withMarkers(result.getMarkers().add(marker));
                }
            }

            return result;
        }
    }
}
