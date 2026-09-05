# UpperEll Recipe

The `UpperEll` recipe fixes Checkstyle UpperEll violations by replacing the lowercase 'l' suffix in `long` integer literals with an uppercase 'L' to improve readability and avoid confusion with the digit '1'.

For more detailed information on this rule, please refer to the official [Checkstyle UpperEll Documentation](https://checkstyle.org/checks/misc/upperell.html).

## Usage

To configure this Checkstyle rule, add the following module to your Checkstyle configuration file (`checkstyle.xml`):

```xml
<module name="Checker">
  <module name="TreeWalker">
    <module name="UpperEll"/>
  </module>
</module>
```

## Example

Below is an example showing how the `UpperEll` recipe transforms your code.

### Input

The following class declares `long` literals using the lowercase 'l' suffix.

```java
public class ExampleClass {
    long value = 1000l;
    long bigNum = 1_000_000l;
}
```

### Output

After applying the recipe, the lowercase 'l' is replaced with uppercase 'L'.

```java
public class ExampleClass {
    long value = 1000L;
    long bigNum = 1_000_000L;
}
```

### Diff

This diff highlights the exact changes made by OpenRewrite:

```diff
--- a/ExampleClass.java
+++ b/ExampleClass.java
@@ -2,3 +2,3 @@
 public class ExampleClass {
-    long value = 1000l;
-    long bigNum = 1_000_000l;
+    long value = 1000L;
+    long bigNum = 1_000_000L;
 }
```
