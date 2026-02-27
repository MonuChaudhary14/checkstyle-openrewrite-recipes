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

package org.checkstyle.autofix.recipe.unusedlocalvariable.recipeenhancements;

import java.util.ArrayList;
import java.util.List;

public class OutputRecipeEnhancements {

    public void redundantAssignment() {
        int a = 1;
        a = 3;
        System.out.println(a);
    }

    public void methodAndNewInitializers() {
        String s = getString(); // (suppressed violation)
        List<String> list = new ArrayList<>(); // (suppressed violation)
        System.out.println("No use of s or list");
    }

    public void nestedBlockAndTypeCast(boolean flag) {
        if (flag) {
        }
        System.out.println(flag);
    }

    private String getString() {
        return "value";
    }
}
