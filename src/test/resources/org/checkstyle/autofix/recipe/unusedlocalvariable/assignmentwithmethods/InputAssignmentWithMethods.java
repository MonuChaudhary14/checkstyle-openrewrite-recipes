/*xml
<module name="Checker">
  <module name="TreeWalker">
    <module name="com.puppycrawl.tools.checkstyle.checks.coding.UnusedLocalVariableCheck"/>
    <module name="com.puppycrawl.tools.checkstyle.filters.SuppressWithNearbyCommentFilter">
      <property name="commentFormat" value="suppressed violation"/>
    </module>
  </module>
</module>

*/

package org.checkstyle.autofix.recipe.unusedlocalvariable.assignmentwithmethods;

public class InputAssignmentWithMethods {

    public void assignment() {
        int a = 1;
        int b = 2; // violation 'Unused named local variable'
        a = 3;
        b = 4;
        System.out.println(a);
    }

    public void methodCall() {
        String s = getString(); // (suppressed violation)
        System.out.println("hello");
    }

    public void complex() {
        Object o = null;
        Object p = new Object(); // (suppressed violation)
        p = getString();
        System.out.println(o);
    }

    private String getString() {
        return "str";
    }
}
