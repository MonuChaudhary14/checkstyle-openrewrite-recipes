/*
com.puppycrawl.tools.checkstyle.checks.imports.CustomImportOrderCheck
customImportOrderRules = SAME_PACKAGE(2)###STATIC###STANDARD_JAVA_PACKAGE###SPECIAL_IMPORTS###THIRD_PARTY_PACKAGE
separateLineBetweenGroups = false
sortImportsInGroupAlphabetically = false
specialImportsRegExp = com\\.puppycrawl\\..*
thirdPartyPackageRegExp = org\\..*

*/

package org.checkstyle.autofix.recipe.customimportorder.basicgroupordering;

import org.checkstyle.autofix.parser.CheckConfiguration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import com.puppycrawl.tools.checkstyle.checks.imports.CustomImportOrderCheck;
import org.junit.jupiter.api.Test;

public class OutputBasicGroupOrdering {
}
