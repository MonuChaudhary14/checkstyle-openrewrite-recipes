# UseEnhancedSwitch Recipe

The `UseEnhancedSwitch` recipe fixes Checkstyle UseEnhancedSwitch violations by converting traditional `switch` statements that use colon (`:`) syntax into enhanced `switch` statements or expressions with arrow (`->`) syntax (introduced in Java 14).

For more detailed information on this rule, please refer to the official [Checkstyle UseEnhancedSwitch Documentation](https://checkstyle.org/checks/coding/useenhancedswitch.html).

## Usage

To configure this Checkstyle rule, add the following module to your Checkstyle configuration file (`checkstyle.xml`):

```xml
<module name="Checker">
  <module name="TreeWalker">
    <module name="UseEnhancedSwitch"/>
  </module>
</module>
```

## Example

Below is an example showing how the `UseEnhancedSwitch` recipe transforms your code.

### Input

The following switch statement uses traditional colon syntax with explicit `break` statements.

```java
public class ExampleClass {
    public void handleAction(int action) {
        switch (action) {
            case 1:
                start();
                break;
            case 2:
                stop();
                break;
            default:
                pause();
                break;
        }
    }
}
```

### Output

After applying the recipe, the switch statement is converted to enhanced arrow syntax without needing `break` statements.

```java
public class ExampleClass {
    public void handleAction(int action) {
        switch (action) {
            case 1 -> start();
            case 2 -> stop();
            default -> pause();
        }
    }
}
```

### Diff

This diff highlights the exact changes made by OpenRewrite:

```diff
--- a/ExampleClass.java
+++ b/ExampleClass.java
@@ -3,11 +3,7 @@
     public void handleAction(int action) {
         switch (action) {
-            case 1:
-                start();
-                break;
-            case 2:
-                stop();
-                break;
-            default:
-                pause();
-                break;
+            case 1 -> start();
+            case 2 -> stop();
+            default -> pause();
         }
```
