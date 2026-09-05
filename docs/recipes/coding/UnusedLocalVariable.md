# UnusedLocalVariable Recipe

The `UnusedLocalVariable` recipe fixes Checkstyle UnusedLocalVariable violations by removing local variable declarations that are never used within their scope, while preserving initializers that contain side effects.

For more detailed information on this rule, please refer to the official [Checkstyle UnusedLocalVariable Documentation](https://checkstyle.org/checks/coding/unusedlocalvariable.html).

## Usage

To configure this Checkstyle rule, add the following module to your Checkstyle configuration file (`checkstyle.xml`):

```xml
<module name="Checker">
  <module name="TreeWalker">
    <module name="UnusedLocalVariable"/>
  </module>
</module>
```

## Example

Below is an example showing how the `UnusedLocalVariable` recipe transforms your code.

### Input

The following method declares an unused local variable `unused`.

```java
public class ExampleClass {
    public void printMessage() {
        int unused = 42;
        String message = "Hello, world!";
        System.out.println(message);
    }
}
```

### Output

After applying the recipe, the unused variable declaration is safely removed.

```java
public class ExampleClass {
    public void printMessage() {
        String message = "Hello, world!";
        System.out.println(message);
    }
}
```

### Diff

This diff highlights the exact changes made by OpenRewrite:

```diff
--- a/ExampleClass.java
+++ b/ExampleClass.java
@@ -2,3 +2,2 @@
     public void printMessage() {
-        int unused = 42;
         String message = "Hello, world!";
```
