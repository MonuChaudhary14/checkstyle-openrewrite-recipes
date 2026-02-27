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

public class InputKillMutations {

    public void unaryTest() {
        int unused = 0; // violation 'Unused named local variable'
        unused++;
        ++unused;
        unused--;
        --unused;

        int dummy = 10;
        int keep1 = dummy++; // (suppressed violation)
        int keep2 = ++dummy; // (suppressed violation)
        int keep3 = dummy--; // (suppressed violation)
        int keep4 = --dummy; // (suppressed violation)

        int unaryPlus = +1; // violation 'Unused named local variable'
        int unaryMinus = -1; // violation 'Unused named local variable'
        int unaryNot = ~1; // violation 'Unused named local variable'
        boolean unaryLogicalNot = !true; // violation 'Unused named local variable'

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
        int noInit; // violation 'Unused named local variable'
    }

    public void mixedBlockTest() {
        int a = 1; // violation 'Unused named local variable'
        System.out.println("Hello");
        a = 2;

        Runnable r = new Runnable() {
            @Override
            public void run() {
                int innerUnused = 5; // violation 'Unused named local variable'
            }
        };
        r.run();
    }
}
