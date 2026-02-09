/*xml
<module name="Checker">
  <module name="TreeWalker">
    <module name="com.puppycrawl.tools.checkstyle.checks.coding.EmptyStatementCheck"/>
  </module>
</module>
*/

package org.checkstyle.autofix.recipe.emptystatement.commentedemptystatement;

public class InputCommentedEmptyStatement {

    public void test(boolean condition) {
        if (condition)
            /* keep me */
            ; // violation 'Empty statement.'
    }
}
