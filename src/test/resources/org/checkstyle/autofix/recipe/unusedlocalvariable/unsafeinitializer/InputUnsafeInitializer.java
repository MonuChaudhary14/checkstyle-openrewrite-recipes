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

package org.checkstyle.autofix.recipe.unusedlocalvariable.unsafeinitializer;

public class InputUnsafeInitializer {

    public void method() {
        int unused = sideEffect(); // (suppressed violation)
    }

    public int sideEffect() {
        System.out.println("Side effect executed!");
        return 42;
    }

    public void anotherMethod() {
        int a = 1, b = sideEffect(), c = 2; // (suppressed violation)
        System.out.println(a + c);
    }

}
