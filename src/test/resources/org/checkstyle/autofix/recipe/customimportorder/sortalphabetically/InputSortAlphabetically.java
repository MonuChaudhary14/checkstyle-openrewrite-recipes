/*
com.puppycrawl.tools.checkstyle.checks.imports.CustomImportOrderCheck
customImportOrderRules = STANDARD_JAVA_PACKAGE
separateLineBetweenGroups = false
sortImportsInGroupAlphabetically = true

*/

package org.checkstyle.autofix.recipe.customimportorder.sortalphabetically;

import java.util.Set;
// violation below 'Wrong lexicographical order for 'java.util.Map' import. Should be before 'java.util.Set'.'
import java.util.Map;
// violation below 'Wrong lexicographical order for 'java.util.List' import. Should be before 'java.util.Set'.'
import java.util.List;

public class InputSortAlphabetically {
}
