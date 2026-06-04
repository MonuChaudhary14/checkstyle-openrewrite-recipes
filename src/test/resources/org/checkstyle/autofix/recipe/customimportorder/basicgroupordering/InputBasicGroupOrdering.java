/*
com.puppycrawl.tools.checkstyle.checks.imports.CustomImportOrderCheck
customImportOrderRules = SAME_PACKAGE(2)###STATIC###STANDARD_JAVA_PACKAGE###SPECIAL_IMPORTS###THIRD_PARTY_PACKAGE
separateLineBetweenGroups = false
sortImportsInGroupAlphabetically = false
specialImportsRegExp = com\\.puppycrawl\\..*
thirdPartyPackageRegExp = org\\..*

*/

package org.checkstyle.autofix.recipe.customimportorder.basicgroupordering;

import org.junit.jupiter.api.Test;
// violation below 'Import statement for 'org.junit.jupiter.api.Assertions.assertEquals' is in the wrong order. Should be in the 'STATIC' group, expecting not assigned imports on this line.'
import static org.junit.jupiter.api.Assertions.assertEquals;
// violation below 'Import statement for 'java.util.List' is in the wrong order. Should be in the 'STANDARD_JAVA_PACKAGE' group, expecting not assigned imports on this line.'
import java.util.List;
// violation below 'Import statement for 'com.puppycrawl.tools.checkstyle.checks.imports.CustomImportOrderCheck' is in the wrong order. Should be in the 'SPECIAL_IMPORTS' group, expecting not assigned imports on this line.'
import com.puppycrawl.tools.checkstyle.checks.imports.CustomImportOrderCheck;
// violation below 'Import statement for 'org.checkstyle.autofix.parser.CheckConfiguration' is in the wrong order. Should be in the 'SAME_PACKAGE' group, expecting not assigned imports on this line.'
import org.checkstyle.autofix.parser.CheckConfiguration;

public class InputBasicGroupOrdering {
}
