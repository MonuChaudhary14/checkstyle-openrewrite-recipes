/*
com.puppycrawl.tools.checkstyle.checks.imports.CustomImportOrderCheck
customImportOrderRules = STATIC###STANDARD_JAVA_PACKAGE###THIRD_PARTY_PACKAGE
separateLineBetweenGroups = true
sortImportsInGroupAlphabetically = false

*/

package org.checkstyle.autofix.recipe.customimportorder.separatelinebetweengroups;

import static org.junit.jupiter.api.Assertions.assertEquals;
// violation below ''java.util.List' should be separated from previous import group by one line.'
import java.util.List;
import java.util.Map;
// violation below ''org.junit.jupiter.api.Test' should be separated from previous import group by one line.'
import org.junit.jupiter.api.Test;

public class InputSeparateLineBetweenGroups {
}
