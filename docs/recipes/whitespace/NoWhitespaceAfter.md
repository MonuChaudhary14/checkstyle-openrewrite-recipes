# NoWhitespaceAfter Recipe

The `NoWhitespaceAfter` recipe fixes Checkstyle NoWhitespaceAfter violations by removing forbidden whitespace after specific tokens such as unary operators (`+`, `-`, `++`, `--`, `~`, `!`), type casts, array indices, method references (`::`), and annotations (`@`).

For more detailed information on this rule, please refer to the official [Checkstyle NoWhitespaceAfter Documentation](https://checkstyle.org/checks/whitespace/nowhitespaceafter.html).

## Usage

To configure this Checkstyle rule, add the following module to your Checkstyle configuration file (`checkstyle.xml`):

```xml
<module name="Checker">
  <module name="TreeWalker">
    <module name="NoWhitespaceAfter"/>
  </module>
</module>
```

## Example

Below is an example showing how the `NoWhitespaceAfter` recipe transforms your code.

### Input

The following class contains unwanted whitespace after unary operators, casts, and method references.

```java
public class ExampleClass {
    public void test(int a) {
        int x = - a;
        boolean flag = ! (a > 0);
        Object obj = (Object) a;
        Runnable r = String:: new;
    }
}
```

### Output

After applying the recipe, whitespace following these tokens is removed.

```java
public class ExampleClass {
    public void test(int a) {
        int x = -a;
        boolean flag = !(a > 0);
        Object obj = (Object)a;
        Runnable r = String::new;
    }
}
```

### Diff

This diff highlights the exact changes made by OpenRewrite:

```diff
--- a/ExampleClass.java
+++ b/ExampleClass.java
@@ -2,6 +2,6 @@
 public class ExampleClass {
     public void test(int a) {
-        int x = - a;
-        boolean flag = ! (a > 0);
-        Object obj = (Object) a;
-        Runnable r = String:: new;
+        int x = -a;
+        boolean flag = !(a > 0);
+        Object obj = (Object)a;
+        Runnable r = String::new;
     }
```
