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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.checkstyle.autofix.parser.CheckConfiguration;
import org.checkstyle.autofix.parser.CheckstyleViolation;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JRightPadded;
import org.openrewrite.java.tree.Space;

public class CustomImportOrder extends Recipe {

    private static final String DOT = ".";
    private static final String DOUBLE_NEWLINE = "\n\n";
    private static final String STATIC_RULE = "STATIC";
    private static final String STANDARD_JAVA_PACKAGE_RULE = "STANDARD_JAVA_PACKAGE";
    private static final String SPECIAL_IMPORTS_RULE = "SPECIAL_IMPORTS";
    private static final String THIRD_PARTY_PACKAGE_RULE = "THIRD_PARTY_PACKAGE";
    private static final String UNMATCHED_GROUP = "UNMATCHED";
    private static final String DOT_REGEX = "\\.";

    private final List<CheckstyleViolation> violations;
    private final CheckConfiguration config;

    public CustomImportOrder(List<CheckstyleViolation> violations, CheckConfiguration config) {
        this.violations = violations;
        this.config = config;
    }

    @Override
    public String getDisplayName() {
        return "CustomImportOrder recipe";
    }

    @Override
    public String getDescription() {
        return "Fixes Checkstyle CustomImportOrder violations by reordering imports.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new CustomImportOrderVisitor(violations, config);
    }

    private static final class CustomImportOrderVisitor extends JavaIsoVisitor<ExecutionContext> {
        private final List<CheckstyleViolation> violations;
        private final String customImportOrderRules;
        private final boolean separateLineBetweenGroups;
        private final boolean sortImportsInGroupAlphabetically;
        private final Pattern standardPackageRegExp;
        private final Pattern thirdPartyPackageRegExp;
        private final Pattern specialImportsRegExp;

        CustomImportOrderVisitor(List<CheckstyleViolation> violations, CheckConfiguration config) {
            this.violations = violations;
            this.customImportOrderRules = config.getPropertyOrDefault("customImportOrderRules", "");
            this.separateLineBetweenGroups = Boolean.parseBoolean(
                    config.getPropertyOrDefault("separateLineBetweenGroups", "true"));
            this.sortImportsInGroupAlphabetically = Boolean.parseBoolean(
                    config.getPropertyOrDefault("sortImportsInGroupAlphabetically", "false"));
            this.standardPackageRegExp = Pattern.compile(
                    config.getPropertyOrDefault("standardPackageRegExp", "^(java|javax)\\."));
            this.thirdPartyPackageRegExp = Pattern.compile(
                    config.getPropertyOrDefault("thirdPartyPackageRegExp", ".*"));
            this.specialImportsRegExp = Pattern.compile(
                    config.getPropertyOrDefault("specialImportsRegExp", "^$"));
        }

        @Override
        public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu,
                                                      ExecutionContext executionContext) {
            final J.CompilationUnit compilationUnit = super.visitCompilationUnit(
                    cu, executionContext);
            J.CompilationUnit resultUnit = compilationUnit;

            if (!compilationUnit.getImports().isEmpty()) {

                String currentPackage = "";
                if (compilationUnit.getPackageDeclaration() != null) {
                    currentPackage = compilationUnit.getPackageDeclaration().getExpression()
                            .printTrimmed(getCursor());
                }

                final List<String> rules = parseRules(customImportOrderRules);

                final Map<JRightPadded<J.Import>, String> importGroups =
                        new IdentityHashMap<>();
                for (JRightPadded<J.Import> imp : compilationUnit.getPadding().getImports()) {
                    final String impString = imp.getElement().getPackageName() + DOT
                            + imp.getElement().getClassName();
                    importGroups.put(imp, determineGroup(imp.getElement().isStatic(), impString,
                            currentPackage, rules));
                }

                final List<JRightPadded<J.Import>> reordered =
                        new ArrayList<>(compilationUnit.getPadding().getImports());

                reordered.sort((impA, impB) -> compareImports(impA, impB, importGroups, rules));

                final List<JRightPadded<J.Import>> formatted = applyFormatting(reordered,
                        importGroups);

                boolean changed = false;
                if (formatted.size() != compilationUnit.getPadding().getImports().size()) {
                    changed = true;
                }
                else {
                    for (int idx = 0; idx < formatted.size(); idx++) {
                        if (formatted.get(idx)
                                != compilationUnit.getPadding().getImports().get(idx)) {
                            changed = true;
                            break;
                        }
                    }
                }

                if (changed) {
                    resultUnit = compilationUnit.getPadding().withImports(formatted);
                }
            }
            return resultUnit;
        }

        private int compareImports(JRightPadded<J.Import> impA, JRightPadded<J.Import> impB,
                                   Map<JRightPadded<J.Import>, String> importGroups,
                                   List<String> rules) {
            final String groupA = importGroups.get(impA);
            final String groupB = importGroups.get(impB);
            final int indexA = getGroupIndex(groupA, rules);
            final int indexB = getGroupIndex(groupB, rules);

            int result = 0;
            if (indexA != indexB) {
                result = Integer.compare(indexA, indexB);
            }
            else if (sortImportsInGroupAlphabetically) {
                final String impStrA = impA.getElement().getPackageName() + DOT
                        + impA.getElement().getClassName();
                final String impStrB = impB.getElement().getPackageName() + DOT
                        + impB.getElement().getClassName();
                result = impStrA.compareTo(impStrB);
            }
            return result;
        }

        private List<JRightPadded<J.Import>> applyFormatting(
                List<JRightPadded<J.Import>> imports,
                Map<JRightPadded<J.Import>, String> importGroups) {

            final List<JRightPadded<J.Import>> result = new ArrayList<>();
            String prevGroup = null;

            for (int idx = 0; idx < imports.size(); idx++) {
                final JRightPadded<J.Import> imp = imports.get(idx);
                final String currentGroup = importGroups.get(imp);

                final Space prefix = imp.getElement().getPrefix();
                String whitespace = prefix.getWhitespace();

                final int lastNewline = whitespace.lastIndexOf('\n');
                final String trailingSpaces;
                if (lastNewline >= 0) {
                    trailingSpaces = whitespace.substring(lastNewline + 1);
                }
                else {
                    trailingSpaces = whitespace;
                }

                if (idx == 0) {
                    whitespace = DOUBLE_NEWLINE + trailingSpaces;
                }
                else {
                    whitespace = "\n" + trailingSpaces;
                    if (prevGroup != null && !prevGroup.equals(currentGroup)
                            && separateLineBetweenGroups) {
                        whitespace = DOUBLE_NEWLINE + trailingSpaces;
                    }
                }

                if (!whitespace.equals(prefix.getWhitespace())) {
                    final J.Import newElement =
                            imp.getElement().withPrefix(prefix.withWhitespace(whitespace));
                    result.add(imp.withElement(newElement));
                }
                else {
                    result.add(imp);
                }
                prevGroup = currentGroup;
            }
            return result;
        }

        private String determineGroup(boolean isStatic, String impString, String currentPackage,
                                      List<String> rules) {
            String matchingGroup = UNMATCHED_GROUP;

            if (isStatic && rules.contains(STATIC_RULE)) {
                matchingGroup = STATIC_RULE;
            }
            else {
                matchingGroup = determineNonStaticGroup(impString, currentPackage, rules);
            }

            return matchingGroup;
        }

        private String determineNonStaticGroup(String impString, String currentPackage,
                                               List<String> rules) {
            String result = UNMATCHED_GROUP;
            for (String rule : rules) {
                if (rule.startsWith("SAME_PACKAGE")
                        && isSamePackage(impString, currentPackage, rule)) {
                    result = rule;
                    break;
                }
                if (STANDARD_JAVA_PACKAGE_RULE.equals(rule)
                        && standardPackageRegExp.matcher(impString).find()) {
                    result = rule;
                    break;
                }
                if (SPECIAL_IMPORTS_RULE.equals(rule)
                        && specialImportsRegExp.matcher(impString).find()) {
                    result = rule;
                    break;
                }
                if (THIRD_PARTY_PACKAGE_RULE.equals(rule)
                        && thirdPartyPackageRegExp.matcher(impString).find()) {
                    result = rule;
                    break;
                }
            }
            return result;
        }

        private boolean isSamePackage(String impString, String currentPackage, String rule) {
            boolean isSame = true;
            if (currentPackage.isEmpty()) {
                isSame = false;
            }
            else {
                int depth = 0;
                // 0 means matching all
                if (rule.contains("(")) {
                    try {
                        depth = Integer.parseInt(
                                rule.substring(rule.indexOf('(') + 1, rule.indexOf(')')));
                    }
                    catch (NumberFormatException exception) {
                        // ignore
                    }
                }

                final String[] impParts = impString.split(DOT_REGEX);
                final String[] currParts = currentPackage.split(DOT_REGEX);

                int limit = currParts.length;
                if (depth != 0) {
                    limit = Math.min(depth, currParts.length);
                }

                if (impParts.length < limit) {
                    isSame = false;
                }
                else {
                    for (int idx = 0; idx < limit; idx++) {
                        if (!impParts[idx].equals(currParts[idx])) {
                            isSame = false;
                            break;
                        }
                    }
                }
            }
            return isSame;
        }

        private int getGroupIndex(String group, List<String> rules) {
            final int index = rules.indexOf(group);
            int result = rules.size();
            if (index >= 0) {
                result = index;
            }
            return result;
        }

        private List<String> parseRules(String rulesString) {
            final List<String> rules = new ArrayList<>();
            for (String rule : rulesString.split("###")) {
                if (!rule.isEmpty()) {
                    rules.add(rule);
                }
            }
            return rules;
        }
    }
}
