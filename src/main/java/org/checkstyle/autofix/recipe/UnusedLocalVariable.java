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

package org.checkstyle.autofix.recipe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.checkstyle.autofix.PositionHelper;
import org.checkstyle.autofix.parser.CheckstyleViolation;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Comment;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.Statement;

/**
 * Fixes Checkstyle UnusedLocalVariable violations by removing local variable
 * declarations
 * that are never used.
 */
public class UnusedLocalVariable extends Recipe {

    private final List<CheckstyleViolation> violations;

    public UnusedLocalVariable(List<CheckstyleViolation> violations) {
        this.violations = violations;
    }

    @Override
    public String getDisplayName() {
        return "UnusedLocalVariable recipe";
    }

    @Override
    public String getDescription() {
        return "Removes pure unused variables while keeping statements with side effects unchanged "
                + "to ensure maximum code safety.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new RemoveUnusedVisitor();
    }

    private final class RemoveUnusedVisitor extends JavaIsoVisitor<ExecutionContext> {

        private static final String QUOTE = "'";

        private final Set<UUID> namedVariablesToRemove = new HashSet<>();
        private Set<String> removedVarNamesInMethod = new HashSet<>();

        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method,
                ExecutionContext executionContext) {
            final Set<String> savedRemovedNames = this.removedVarNamesInMethod;
            this.removedVarNamesInMethod = new HashSet<>();
            final J.MethodDeclaration result =
                    super.visitMethodDeclaration(method, executionContext);
            this.removedVarNamesInMethod = savedRemovedNames;
            return result;
        }

        @Override
        public J.VariableDeclarations visitVariableDeclarations(J.VariableDeclarations varDecl,
                ExecutionContext executionContext) {
            final J.VariableDeclarations visited =
                    super.visitVariableDeclarations(varDecl, executionContext);

            for (J.VariableDeclarations.NamedVariable variable : visited.getVariables()) {
                if (isAtViolationLocation(visited, variable)) {
                    namedVariablesToRemove.add(variable.getId());
                    removedVarNamesInMethod.add(variable.getSimpleName());
                }
            }
            return visited;
        }

        @Override
        public J.Block visitBlock(J.Block block, ExecutionContext executionContext) {
            final J.Block visited = super.visitBlock(block, executionContext);
            return visited.withStatements(buildNewStatements(visited.getStatements()));
        }

        @Override
        public J.Case visitCase(J.Case _case, ExecutionContext executionContext) {
            final J.Case visited = super.visitCase(_case, executionContext);
            return visited.withStatements(buildNewStatements(visited.getStatements()));
        }

        private boolean isAtViolationLocation(J.VariableDeclarations varDecl,
                J.VariableDeclarations.NamedVariable variable) {
            final J.CompilationUnit cu = getCursor().firstEnclosing(J.CompilationUnit.class);

            boolean matches = false;
            if (cu != null) {
                final int line = PositionHelper
                        .computeLinePosition(cu, varDecl, getCursor());

                for (CheckstyleViolation violation : violations) {
                    final String violationPath = violation.getFilePath().toString();
                    if (violation.getLine() == line
                            && violationPath.endsWith(cu.getSourcePath().toString())
                            && violation.getMessage()
                                    .contains(QUOTE + variable.getSimpleName() + QUOTE)) {
                        matches = true;
                        break;
                    }
                }
            }
            return matches;
        }

        private List<Statement> buildNewStatements(List<Statement> statements) {
            final List<Statement> newStatements = new ArrayList<>();
            List<Comment> pendingComments = new ArrayList<>();
            for (Statement stmt : statements) {
                if (stmt instanceof J.VariableDeclarations varDecl) {
                    handleVariableDeclaration(varDecl, newStatements, pendingComments);
                }
                else if (!isOrphanedAssignment(stmt, removedVarNamesInMethod)) {
                    Statement stmtToAdd = stmt;
                    if (!pendingComments.isEmpty()) {
                        stmtToAdd = prependComments(stmt, pendingComments);
                        pendingComments = new ArrayList<>();
                    }
                    newStatements.add(stmtToAdd);
                }
                else {
                    pendingComments.addAll(extractComments(stmt));
                }
            }
            return newStatements;
        }

        private static Statement prependComments(Statement stmt, List<Comment> comments) {
            final Space prefix = stmt.getPrefix();
            final List<Comment> merged = new ArrayList<>(comments);
            merged.addAll(prefix.getComments());
            return stmt.withPrefix(prefix.withComments(merged));
        }

        private static List<Comment> extractComments(Statement stmt) {
            return stmt.getPrefix().getComments();
        }

        private static boolean isOrphanedAssignment(Statement stmt, Set<String> removedNames) {
            String name = null;
            if (stmt instanceof J.Assignment assignment
                    && assignment.getVariable() instanceof J.Identifier id) {
                name = id.getSimpleName();
            }
            else if (stmt instanceof J.AssignmentOperation assignmentOp
                    && assignmentOp.getVariable() instanceof J.Identifier id) {
                name = id.getSimpleName();
            }
            else if (stmt instanceof J.Unary unary
                    && unary.getExpression() instanceof J.Identifier id) {
                name = id.getSimpleName();
            }
            return name != null && removedNames.contains(name);
        }

        private void handleVariableDeclaration(J.VariableDeclarations varDecl,
                List<Statement> newStatements, List<Comment> pendingComments) {
            final List<J.VariableDeclarations.NamedVariable> remaining = new ArrayList<>();

            for (J.VariableDeclarations.NamedVariable variable : varDecl.getVariables()) {
                if (namedVariablesToRemove.contains(variable.getId())) {
                    final Expression initializer = variable.getInitializer();
                    if (initializer != null && hasSideEffects(initializer)) {
                        remaining.add(variable);
                    }
                }
                else {
                    remaining.add(variable);
                }
            }
            if (!remaining.isEmpty()) {
                Statement toAdd;
                if (remaining.size() == varDecl.getVariables().size()) {
                    toAdd = varDecl;
                }
                else {
                    toAdd = varDecl.withVariables(remaining);
                }
                if (!pendingComments.isEmpty()) {
                    toAdd = prependComments(toAdd, pendingComments);
                    pendingComments.clear();
                }
                newStatements.add(toAdd);
            }
            else {
                pendingComments.addAll(extractComments(varDecl));
            }
        }

        private static boolean hasSideEffects(Expression expression) {
            return expression instanceof J.MethodInvocation
                    || expression instanceof J.NewClass
                    || expression instanceof J.Assignment
                    || expression instanceof J.AssignmentOperation
                    || expression instanceof J.Unary unary && isPostOrPreIncrement(unary);
        }

        private static boolean isPostOrPreIncrement(J.Unary unary) {
            return unary.getOperator() == J.Unary.Type.PreIncrement
                    || unary.getOperator() == J.Unary.Type.PreDecrement
                    || unary.getOperator() == J.Unary.Type.PostIncrement
                    || unary.getOperator() == J.Unary.Type.PostDecrement;
        }

    }
}
