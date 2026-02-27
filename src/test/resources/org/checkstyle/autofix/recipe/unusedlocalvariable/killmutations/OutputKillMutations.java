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

package org.checkstyle.autofix.recipe.unusedlocalvariable.killmutations;

public class OutputKillMutations {

    public void unaryTest() {
        int dummy = 10;
        int keep1 = dummy++; // (suppressed violation)
        int keep2 = ++dummy; // (suppressed violation)
        int keep3 = dummy--; // (suppressed violation)
        int keep4 = --dummy; // (suppressed violation)

        System.out.println(dummy);
    }

    public void assignmentTest() {
        int dummy = 10;
        int keep5 = dummy += 5; // (suppressed violation)
        int keep6 = dummy = 20; // (suppressed violation)

        int assignOp = 0;
        assignOp += 5;

        System.out.println(dummy);
    }

    public void noInitTest() {
    }

    public void mixedBlockTest() {
        System.out.println("Hello");

        Runnable r = new Runnable() {
            @Override
            public void run() {
            }
        };
        r.run();
    }
}
