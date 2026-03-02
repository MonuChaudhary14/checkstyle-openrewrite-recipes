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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.checkstyle.autofix.PositionHelper;
import org.checkstyle.autofix.parser.CheckstyleViolation;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JContainer;
import org.openrewrite.java.tree.JLeftPadded;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.marker.Markers;

/**
 * Fixes Checkstyle UseEnhancedSwitch violations by converting traditional switch
 * statements using colon syntax to enhanced switch using arrow syntax.
 */
public class UseEnhancedSwitch extends Recipe {

    private final List<CheckstyleViolation> violations;

    public UseEnhancedSwitch(List<CheckstyleViolation> violations) {
        this.violations = violations;
    }

    @Override
    public String getDisplayName() {
        return "UseEnhancedSwitch recipe";
    }

    @Override
    public String getDescription() {
        return "Convert switch statements using colon syntax to enhanced switch "
                + "using arrow syntax.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new UseEnhancedSwitchVisitor();
    }

    private final class UseEnhancedSwitchVisitor extends JavaVisitor<ExecutionContext> {

        private static final String NEWLINE = "\n";
        private static final String DEFAULT_LABEL = "default";
        private Path sourcePath;

        @Override
        public J.CompilationUnit visitCompilationUnit(
                J.CompilationUnit cu, ExecutionContext executionContext) {
            this.sourcePath = cu.getSourcePath();
            return (J.CompilationUnit) super.visitCompilationUnit(cu, executionContext);
        }

        private boolean isTraditional(J.Block cases) {
            return cases.getStatements().stream()
                    .allMatch(stmt -> {
                        return !(stmt instanceof J.Case caseStmt
                                && caseStmt.getType() == J.Case.Type.Rule);
                    });
        }

        @Override
        public Statement visitSwitch(J.Switch switchNode, ExecutionContext executionContext) {
            final J.Switch visited = (J.Switch) super.visitSwitch(switchNode, executionContext);
            Statement finalResult = visited;

            if (isTraditional(visited.getCases())
                    && isAtViolationLocation(visited)) {
                final List<Statement> mergedCases =
                        mergeFallthroughCases(visited.getCases().getStatements());
                final boolean isExpr = canConvertToReturnSwitchExpression(
                        visited.getSelector(), mergedCases);

                final J.Switch updatedSwitch = visited.withCases(
                        convertCases(visited.getCases(), visited.getSelector(), isExpr));

                if (isExpr) {
                    finalResult = new J.Return(
                            org.openrewrite.Tree.randomId(),
                            updatedSwitch.getPrefix(),
                            org.openrewrite.marker.Markers.EMPTY,
                            new J.SwitchExpression(
                                    org.openrewrite.Tree.randomId(),
                                    Space.SINGLE_SPACE,
                                    org.openrewrite.marker.Markers.EMPTY,
                                    updatedSwitch.getSelector(),
                                    updatedSwitch.getCases(),
                                    null));
                }
                else {
                    finalResult = updatedSwitch;
                }
            }

            return finalResult;
        }

        @Override
        public J.Block visitBlock(J.Block block, ExecutionContext executionContext) {
            final J.Block visited = (J.Block) super.visitBlock(block, executionContext);
            return mergeAssignmentSwitches(visited);
        }

        private J.Block mergeAssignmentSwitches(J.Block visited) {
            final List<Statement> statements = visited.getStatements();
            final List<Statement> newStatements = new ArrayList<>();
            boolean changed = false;
            final java.util.Set<Integer> skipIndices = new java.util.HashSet<>();

            for (int idx = 0; idx < statements.size(); idx++) {
                if (skipIndices.contains(idx)) {
                    continue;
                }
                final Statement stmt = statements.get(idx);
                final boolean isCandidate = idx < statements.size() - 1
                        && stmt instanceof J.VariableDeclarations
                        && statements.get(idx + 1) instanceof J.Switch;
                if (isCandidate) {
                    final J.VariableDeclarations varDecl = (J.VariableDeclarations) stmt;
                    final J.Switch switchStmt = (J.Switch) statements.get(idx + 1);
                    if (varDecl.getVariables().size() == 1
                            && varDecl.getVariables().get(0).getInitializer() == null) {
                        final String varName = varDecl.getVariables().get(0)
                                .getName().getSimpleName();
                        final List<Statement> mergedCases = mergeFallthroughCases(
                                switchStmt.getCases().getStatements());
                        if (canConvertToAssignmentSwitchExpression(
                                switchStmt.getSelector(), mergedCases, varName)) {
                            newStatements.add(
                                    convertToAssignmentSwitchExpression(
                                            varDecl, switchStmt));
                            skipIndices.add(idx + 1);
                            changed = true;
                            continue;
                        }
                    }
                }
                newStatements.add(stmt);
            }

            J.Block result = visited;
            if (changed) {
                result = visited.withStatements(newStatements);
            }
            return result;
        }

        private boolean isValidAssignmentCase(J.Case caseStmt, String varName) {
            final List<Statement> caseStatements = getMeaningfulStatements(caseStmt);
            boolean result = false;
            if (!caseStatements.isEmpty()) {
                final Statement lastStmt = caseStatements.get(caseStatements.size() - 1);

                result = lastStmt instanceof J.Throw
                        || lastStmt instanceof J.Assignment asgn
                                && asgn.getVariable() instanceof J.Identifier ident
                                && varName.equals(ident.getSimpleName());
            }
            return result;
        }

        private J.VariableDeclarations convertToAssignmentSwitchExpression(
                J.VariableDeclarations varDecl, J.Switch switchStmt) {
            final String varName = varDecl.getVariables().get(0).getName().getSimpleName();
            final J.Block newCases = convertAssignmentCases(switchStmt.getCases(), varName);

            final J.SwitchExpression switchExpr = new J.SwitchExpression(
                    org.openrewrite.Tree.randomId(),
                    Space.SINGLE_SPACE,
                    org.openrewrite.marker.Markers.EMPTY,
                    switchStmt.getSelector(),
                    newCases,
                    null);

            J.VariableDeclarations.NamedVariable namedVar = varDecl.getVariables().get(0);
            final JLeftPadded<Expression> initializer = JLeftPadded
                    .build((Expression) switchExpr)
                    .withBefore(Space.SINGLE_SPACE);
            namedVar = namedVar.getPadding().withInitializer(initializer);

            final List<J.VariableDeclarations.NamedVariable> variables =
                    new ArrayList<>(varDecl.getVariables());
            variables.set(0, namedVar);
            return varDecl.withVariables(variables);
        }

        private J.Block convertAssignmentCases(J.Block casesBlock, String varName) {
            final List<Statement> casesToProcess =
                    mergeFallthroughCases(casesBlock.getStatements());

            final List<Statement> newStatements = new ArrayList<>();
            for (Statement stmt : casesToProcess) {
                newStatements.add(convertAssignmentCase((J.Case) stmt, varName));
            }

            return casesBlock.withStatements(newStatements);
        }

        private J.Case convertAssignmentCase(J.Case caseStmt, String varName) {
            final J.Case result;
            if (caseStmt.getType() == J.Case.Type.Rule) {
                result = convertArrowAssignmentCase(caseStmt, varName);
            }
            else {
                final List<Statement> statements = caseStmt.getStatements();
                if (statements.size() == 1
                        && statements.get(0) instanceof J.Block block) {
                    result = convertAssignmentBlockCase(caseStmt, block, varName);
                }
                else {
                    final List<Statement> sorted = removeBreak(statements);
                    if (sorted.size() == 1
                            && !(sorted.get(0) instanceof J.Switch)) {
                        result = convertToSingleAssignmentArrow(
                                caseStmt, sorted.get(0), varName);
                    }
                    else {
                        result = convertToBlockAssignmentArrow(caseStmt, sorted, varName);
                    }
                }
            }
            return result;
        }

        private J.Case convertArrowAssignmentCase(
                J.Case caseStmt, String varName) {
            final J body = caseStmt.getBody();
            J.Case result = caseStmt;
            if (body instanceof J.Block block) {
                final List<Statement> cleaned = removeBreak(block.getStatements());
                final J.Block newBlock = block
                        .withStatements(
                                adjustForAssignment(cleaned, varName));
                result = caseStmt.withBody(newBlock);
            }
            else if (body instanceof J.Assignment asgn
                    && asgn.getVariable() instanceof J.Identifier ident
                    && varName.equals(ident.getSimpleName())) {
                result = caseStmt.withBody(
                        asgn.getAssignment()
                                .withPrefix(Space.SINGLE_SPACE));
            }

            return result;
        }

        private J.Case convertAssignmentBlockCase(J.Case caseStmt,
                J.Block block, String varName) {
            final List<Statement> cleaned = removeBreak(block.getStatements());
            final J.Case result;
            if (cleaned.size() == 1
                    && !(cleaned.get(0) instanceof J.Switch)) {
                result = convertToSingleAssignmentArrow(
                        caseStmt, cleaned.get(0), varName);
            }
            else {
                final Space casePrefix = caseStmt.getPrefix();
                final int indentDelta = extractIndentWidth(block.getPrefix())
                        - extractIndentWidth(casePrefix);
                final List<Statement> adjusted = adjustIndentation(cleaned, indentDelta);
                final Space endSpace = createBlockEndSpace(casePrefix);
                result = setArrowType(caseStmt).withBody(block
                        .withPrefix(Space.SINGLE_SPACE)
                        .withStatements(
                                adjustForAssignment(adjusted, varName))
                        .withEnd(endSpace));
            }
            return result;
        }

        private J.Case convertToSingleAssignmentArrow(
                J.Case caseStmt, Statement stmt, String varName) {
            J body = stmt;
            if (stmt instanceof J.Assignment asgn
                    && asgn.getVariable() instanceof J.Identifier ident
                    && varName.equals(ident.getSimpleName())) {
                body = asgn.getAssignment();
            }
            return setArrowType(caseStmt).withBody(body.withPrefix(Space.SINGLE_SPACE));
        }

        private J.Case convertToBlockAssignmentArrow(
                J.Case caseStmt, List<Statement> statements, String varName) {
            final Space casePrefix = caseStmt.getPrefix();
            final Space endSpace;
            if (casePrefix.getWhitespace().contains(NEWLINE)) {
                endSpace = casePrefix;
            }
            else {
                endSpace = Space.EMPTY;
            }

            return setArrowType(caseStmt).withBody(J.Block.createEmptyBlock()
                    .withPrefix(Space.SINGLE_SPACE)
                    .withStatements(
                            adjustForAssignment(
                                    new ArrayList<>(statements), varName))
                    .withEnd(endSpace));
        }

        private List<Statement> adjustForAssignment(
                List<Statement> statements, String varName) {
            final List<Statement> newStatements = new ArrayList<>(statements);
            final int lastIdx = newStatements.size() - 1;
            final Statement lastStmt = newStatements.get(lastIdx);

            if (lastStmt instanceof J.Assignment asgn
                    && asgn.getVariable() instanceof J.Identifier ident
                    && varName.equals(ident.getSimpleName())) {
                newStatements.set(lastIdx, new J.Yield(
                        org.openrewrite.Tree.randomId(),
                        lastStmt.getPrefix(),
                        Markers.EMPTY,
                        false,
                        asgn.getAssignment()));
            }

            return newStatements;
        }

        @Override
        public J.SwitchExpression visitSwitchExpression(
                J.SwitchExpression switchExpression, ExecutionContext executionContext) {
            final J.SwitchExpression visited = (J.SwitchExpression) super.visitSwitchExpression(
                    switchExpression, executionContext);
            J.SwitchExpression result = visited;

            if (isTraditional(visited.getCases())
                    && isAtViolationLocation(visited)) {
                result = visited.withCases(
                        convertCases(visited.getCases(), visited.getSelector(), true));
            }

            return result;
        }

        private boolean canConvertToAssignmentSwitchExpression(
                J.ControlParentheses<Expression> selector,
                List<Statement> mergedCases, String varName) {
            boolean hasDefault = false;
            boolean valid = true;

            for (Statement stmt : mergedCases) {
                final J.Case caseStmt = (J.Case) stmt;
                if (isDefaultCase(caseStmt)) {
                    hasDefault = true;
                }

                if (!isValidAssignmentCase(caseStmt, varName)) {
                    valid = false;
                    break;
                }
            }

            return valid && (hasDefault || isEnumExhaustive(selector, mergedCases));
        }

        private boolean canConvertToReturnSwitchExpression(
                J.ControlParentheses<Expression> selector, List<Statement> mergedCases) {
            boolean hasDefault = false;
            boolean hasAtLeastOneReturn = false;
            boolean valid = true;

            for (Statement stmt : mergedCases) {
                final J.Case caseStmt = (J.Case) stmt;
                if (isDefaultCase(caseStmt)) {
                    hasDefault = true;
                }

                if (!isValidReturnCase(caseStmt)) {
                    valid = false;
                    break;
                }

                final List<Statement> caseStatements = getMeaningfulStatements(caseStmt);
                final Statement lastStmt = caseStatements.get(caseStatements.size() - 1);
                if (lastStmt instanceof J.Return) {
                    hasAtLeastOneReturn = true;
                }
            }

            return valid && (hasDefault || isEnumExhaustive(selector, mergedCases))
                    && hasAtLeastOneReturn;
        }

        private boolean isValidReturnCase(J.Case caseStmt) {
            final List<Statement> caseStatements = getMeaningfulStatements(caseStmt);
            boolean result = false;
            if (!caseStatements.isEmpty()) {
                final Statement lastStmt = caseStatements.get(caseStatements.size() - 1);

                result = lastStmt instanceof J.Throw
                        || lastStmt instanceof J.Return ret && ret.getExpression() != null;
            }
            return result;
        }

        private List<Statement> getMeaningfulStatements(J.Case caseStmt) {
            List<Statement> statements;
            if (caseStmt.getType() == J.Case.Type.Rule) {
                final J body = caseStmt.getBody();
                if (body instanceof J.Block block) {
                    statements = new ArrayList<>(block.getStatements());
                    statements.removeIf(stm -> {
                        return stm instanceof J.Break brk && brk.getLabel() == null;
                    });
                }
                else {
                    statements = Collections.singletonList((Statement) body);
                }
            }
            else {
                statements = new ArrayList<>(caseStmt.getStatements());
                statements.removeIf(stm -> {
                    return stm instanceof J.Break brk && brk.getLabel() == null;
                });
                if (statements.size() == 1 && statements.get(0) instanceof J.Block block) {
                    statements = new ArrayList<>(block.getStatements());
                    statements.removeIf(stm -> {
                        return stm instanceof J.Break brk && brk.getLabel() == null;
                    });
                }
            }
            return statements;
        }

        private boolean isDefaultCase(J.Case caseStmt) {
            boolean result = false;
            for (J label : caseStmt.getCaseLabels()) {
                if (label instanceof J.Identifier ident
                        && DEFAULT_LABEL.equals(ident.getSimpleName())) {
                    result = true;
                    break;
                }
            }
            return result;
        }

        private J.Block convertCases(
                J.Block casesBlock, J.ControlParentheses<Expression> selector,
                boolean isExpr) {
            final List<Statement> mergedCases = mergeFallthroughCases(casesBlock.getStatements());
            final List<Statement> newStatements = new ArrayList<>();

            for (Statement stmt : mergedCases) {
                newStatements.add(convertCase((J.Case) stmt, isExpr));
            }

            final List<Statement> finalStatements;
            if (isExpr && isEnumExhaustive(selector, mergedCases)) {
                finalStatements = removeDefaultCase(newStatements);
            }
            else {
                finalStatements = newStatements;
            }

            return casesBlock.withStatements(finalStatements);
        }

        private boolean isEnumExhaustive(
                J.ControlParentheses<Expression> selector,
                List<Statement> mergedCases) {
            final JavaType selectorType = selector.getTree().getType();
            boolean result = false;

            if (selectorType instanceof JavaType.FullyQualified fq
                    && fq.getKind() == JavaType.FullyQualified.Kind.Enum) {
                final Set<String> enumConstants = fq.getMembers().stream()
                        .filter(member -> {
                            return member.hasFlags(org.openrewrite.java.tree.Flag.Enum);
                        })
                        .map(JavaType.Variable::getName)
                        .collect(java.util.stream.Collectors.toSet());

                final Set<String> caseLabels = new HashSet<>();
                for (Statement stmt : mergedCases) {
                    final J.Case caseStmt = (J.Case) stmt;
                    for (J label : caseStmt.getCaseLabels()) {
                        if (label instanceof J.Identifier ident
                                && !DEFAULT_LABEL.equals(
                                        ident.getSimpleName())) {
                            caseLabels.add(ident.getSimpleName());
                        }
                    }
                }

                result = !enumConstants.isEmpty()
                        && caseLabels.containsAll(enumConstants);
            }
            return result;
        }

        private List<Statement> removeDefaultCase(List<Statement> statements) {
            final List<Statement> result = new ArrayList<>();
            for (Statement stmt : statements) {
                if (!(stmt instanceof J.Case caseStmt)
                        || !isDefaultCase(caseStmt)) {
                    result.add(stmt);
                }
            }
            return result;
        }

        private List<Statement> mergeFallthroughCases(List<Statement> statements) {
            final List<Statement> result = new ArrayList<>();
            final List<J.Case> pendingCases = new ArrayList<>();

            for (Statement stmt : statements) {
                final J.Case caseStmt = (J.Case) stmt;

                if (caseStmt.getType() == J.Case.Type.Rule) {
                    result.add(caseStmt);
                }
                else if (caseStmt.getStatements().isEmpty()) {
                    pendingCases.add(caseStmt);
                }
                else {
                    pendingCases.add(caseStmt);
                    result.add(buildMergedCase(pendingCases));
                }
            }

            if (!pendingCases.isEmpty()) {
                result.add(buildMergedCase(pendingCases));
            }
            return result;
        }

        private J.Case buildMergedCase(List<J.Case> pendingCases) {
            final J.Case last = pendingCases.get(pendingCases.size() - 1);
            final List<J> resultLabels;
            if (isDefaultCase(last)) {
                resultLabels = last.getCaseLabels();
            }
            else {
                resultLabels = new ArrayList<>();
                for (J.Case pending : pendingCases) {
                    resultLabels.addAll(pending.getCaseLabels());
                }
            }
            pendingCases.clear();
            return last.withCaseLabels(resultLabels);
        }

        private J.Case convertCase(J.Case caseStmt, boolean isExpr) {
            final J.Case result;
            final List<Statement> statements = caseStmt.getStatements();
            if (statements.size() == 1
                    && statements.get(0) instanceof J.Block block) {
                result = convertBlockCase(caseStmt, block, isExpr);
            }
            else {
                final List<Statement> sorted = removeBreak(statements);
                if (sorted.size() == 1
                        && !(sorted.get(0) instanceof J.Switch)
                        && isValidSingleStatementArrow(sorted.get(0), isExpr)) {
                    result = convertToSingleStatementArrow(
                            caseStmt, sorted.get(0), isExpr);
                }
                else {
                    result = convertToBlockArrow(caseStmt, sorted, isExpr);
                }
            }
            return result;
        }

        private J.Case convertBlockCase(J.Case caseStmt,
                J.Block block, boolean isExpr) {
            final J.Case result;
            final List<Statement> cleaned = removeBreak(block.getStatements());
            if (cleaned.size() == 1
                    && !(cleaned.get(0) instanceof J.Switch)
                    && isValidSingleStatementArrow(cleaned.get(0), isExpr)) {
                result = convertToSingleStatementArrow(
                        caseStmt, cleaned.get(0), isExpr);
            }
            else {
                final Space casePrefix = caseStmt.getPrefix();
                final int indentDelta = extractIndentWidth(block.getPrefix())
                        - extractIndentWidth(casePrefix);
                final List<Statement> adjusted = adjustIndentation(cleaned, indentDelta);
                final Space endSpace;
                if (adjusted.isEmpty()) {
                    endSpace = Space.EMPTY;
                }
                else {
                    endSpace = createBlockEndSpace(casePrefix);
                }
                result = setArrowType(caseStmt).withBody(block
                        .withPrefix(Space.SINGLE_SPACE)
                        .withStatements(adjustForExpression(adjusted, isExpr))
                        .withEnd(endSpace));
            }
            return result;
        }

        private boolean isValidSingleStatementArrow(Statement stmt, boolean isExpr) {
            J target = stmt;
            if (stmt instanceof J.Yield yield) {
                target = yield.getValue();
            }
            else if (isExpr && stmt instanceof J.Return ret
                    && ret.getExpression() != null) {
                target = ret.getExpression();
            }
            return target instanceof Expression || target instanceof J.Throw;
        }

        private int extractIndentWidth(Space space) {
            final String ws = space.getWhitespace();
            final int lastNewline = ws.lastIndexOf('\n');
            return ws.substring(lastNewline + 1).length();
        }

        private Space createBlockEndSpace(Space casePrefix) {
            final String ws = casePrefix.getWhitespace();
            final Space result;
            if (ws.contains(NEWLINE)) {
                result = Space.build(ws, Collections.emptyList());
            }
            else {
                result = Space.EMPTY;
            }
            return result;
        }

        private List<Statement> adjustIndentation(
                List<Statement> stmts, int delta) {
            final List<Statement> result = new ArrayList<>();
            for (Statement stmt : stmts) {
                result.add(adjustStatementIndent(stmt, delta));
            }
            return result;
        }

        private Statement adjustStatementIndent(
                Statement stmt, int delta) {
            final String ws = stmt.getPrefix().getWhitespace();
            final int lastNewline = ws.lastIndexOf('\n');
            Statement result = stmt;
            if (lastNewline >= 0) {
                final String beforeNewline = ws.substring(0, lastNewline + 1);
                final String indent = ws.substring(lastNewline + 1);
                final int newWidth = Math.max(0, indent.length() - delta);
                result = stmt.withPrefix(Space.format(
                        beforeNewline + " ".repeat(newWidth)));
            }
            return result;
        }

        private List<Statement> adjustForExpression(
                List<Statement> statements, boolean isExpr) {
            final List<Statement> newStatements = new ArrayList<>(statements);
            if (isExpr) {
                final int lastIdx = newStatements.size() - 1;
                final Statement lastStmt = newStatements.get(lastIdx);

                if (lastStmt instanceof J.Return ret && ret.getExpression() != null) {
                    newStatements.set(lastIdx, new J.Yield(
                            org.openrewrite.Tree.randomId(),
                            lastStmt.getPrefix(),
                            Markers.EMPTY,
                            false,
                            ret.getExpression()));
                }
            }
            return newStatements;
        }

        private J.Case convertToSingleStatementArrow(
                J.Case caseStmt, Statement stmt, boolean isExpr) {
            J body = stmt;
            if (stmt instanceof J.Yield yield) {
                body = yield.getValue();
            }
            else if (isExpr && stmt instanceof J.Return ret
                    && ret.getExpression() != null) {
                body = ret.getExpression();
            }
            return setArrowType(caseStmt).withBody(body.withPrefix(Space.SINGLE_SPACE));
        }

        private J.Case convertToBlockArrow(
                J.Case caseStmt, List<Statement> statements, boolean isExpr) {
            final Space endSpace;
            if (statements.isEmpty()) {
                endSpace = Space.EMPTY;
            }
            else {
                endSpace = createBlockEndSpace(caseStmt.getPrefix());
            }
            return setArrowType(caseStmt).withBody(J.Block.createEmptyBlock()
                    .withPrefix(Space.SINGLE_SPACE)
                    .withStatements(adjustForExpression(new ArrayList<>(statements), isExpr))
                    .withEnd(endSpace));
        }

        private J.Case setArrowType(J.Case caseStmt) {
            return caseStmt.withType(J.Case.Type.Rule)
                    .getPadding().withStatements(JContainer.build(
                            Space.SINGLE_SPACE, new java.util.ArrayList<>(),
                            Markers.EMPTY));
        }

        private List<Statement> removeBreak(List<Statement> statements) {
            final List<Statement> filtered = new ArrayList<>();
            for (Statement stmt : statements) {
                if (!(stmt instanceof J.Break breakStmt) || breakStmt.getLabel() != null) {
                    filtered.add(stmt);
                }
            }
            return filtered;
        }

        private boolean isAtViolationLocation(J switchNode) {
            boolean result = false;
            if (violations != null) {
                final J.CompilationUnit cu = getCursor().firstEnclosing(J.CompilationUnit.class);
                if (cu != null) {
                    final int line = PositionHelper.computeLinePosition(cu, switchNode,
                            getCursor());
                    final int column = PositionHelper.computeColumnPosition(cu, switchNode,
                            getCursor());

                    for (CheckstyleViolation violation : violations) {
                        if (violation.getLine() == line
                                && violation.getColumn() == column
                                && violation.getFilePath().endsWith(
                                        sourcePath.toString())) {
                            result = true;
                            break;
                        }
                    }
                }
            }
            else {
                // when running without checkstyle violations context
                result = true;
            }
            return result;
        }
    }
}
