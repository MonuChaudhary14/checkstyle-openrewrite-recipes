# UnnecessaryParentheses Recipe

The `UnnecessaryParentheses` recipe fixes Checkstyle UnnecessaryParentheses violations by removing redundant parentheses around expressions, identifiers, and return values where operator precedence or language syntax already makes the meaning unambiguous.

For more detailed information on this rule, please refer to the official [Checkstyle UnnecessaryParentheses Documentation](https://checkstyle.org/checks/coding/unnecessaryparentheses.html).

## Usage

To configure this Checkstyle rule, add the following module to your Checkstyle configuration file (`checkstyle.xml`):

```xml
<module name="Checker">
  <module name="TreeWalker">
    <module name="UnnecessaryParentheses"/>
  </module>
</module>
```

## Example

Below is an example showing how the `UnnecessaryParentheses` recipe transforms your code.

### Input

The following method contains redundant parentheses in variable assignments and the return statement.

```java
public class ExampleClass {
    public int calculate(int a, int b) {
        int product = (a * b);
        int total = (product) + 10;
        return (total);
    }
}
```

### Output

After applying the recipe, redundant parentheses are removed.

```java
public class ExampleClass {
    public int calculate(int a, int b) {
        int product = a * b;
        int total = product + 10;
        return total;
    }
}
```

### Diff

This diff highlights the exact changes made by OpenRewrite:

```diff
--- a/ExampleClass.java
+++ b/ExampleClass.java
@@ -2,5 +2,5 @@
     public int calculate(int a, int b) {
-        int product = (a * b);
-        int total = (product) + 10;
-        return (total);
+        int product = a * b;
+        int total = product + 10;
+        return total;
     }
```
