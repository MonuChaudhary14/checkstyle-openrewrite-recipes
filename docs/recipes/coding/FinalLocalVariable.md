# FinalLocalVariable Recipe

The `FinalLocalVariable` recipe fixes Checkstyle FinalLocalVariable violations by adding the `final` modifier to local variables that are never reassigned.

For more detailed information on this rule, please refer to the official [Checkstyle FinalLocalVariable Documentation](https://checkstyle.org/checks/coding/finallocalvariable.html).

## Usage

To configure this Checkstyle rule, add the following module to your Checkstyle configuration file (`checkstyle.xml`):

```xml
<module name="Checker">
  <module name="TreeWalker">
    <module name="FinalLocalVariable"/>
  </module>
</module>
```

## Example

Below is an example showing how the `FinalLocalVariable` recipe transforms your code.

### Input

The following method declares local variables that are never modified after initialization, but are not declared as `final`.

```java
public class ExampleClass {
    public void calculate() {
        int x = 10;
        int y = 20;
        int sum = x + y;
    }
}
```

### Output

After applying the recipe, the variables are declared with the `final` modifier.

```java
public class ExampleClass {
    public void calculate() {
        final int x = 10;
        final int y = 20;
        final int sum = x + y;
    }
}
```

### Diff

This diff highlights the exact changes made by OpenRewrite:

```diff
--- a/ExampleClass.java
+++ b/ExampleClass.java
@@ -2,4 +2,4 @@
     public void calculate() {
-        int x = 10;
-        int y = 20;
-        int sum = x + y;
+        final int x = 10;
+        final int y = 20;
+        final int sum = x + y;
     }
```
