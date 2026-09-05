# EmptyStatement Recipe

The `EmptyStatement` recipe fixes Checkstyle EmptyStatement violations by removing empty statements (standalone semicolons) from code or replacing them with empty blocks where statements are expected in control structures.

For more detailed information on this rule, please refer to the official [Checkstyle EmptyStatement Documentation](https://checkstyle.org/checks/coding/emptystatement.html).

## Usage

To configure this Checkstyle rule, add the following module to your Checkstyle configuration file (`checkstyle.xml`):

```xml
<module name="Checker">
  <module name="TreeWalker">
    <module name="EmptyStatement"/>
  </module>
</module>
```

## Example

Below is an example showing how the `EmptyStatement` recipe transforms your code.

### Input

The following class contains standalone semicolons and an empty statement as a branch body.

```java
public class ExampleClass {
    public void example(boolean condition) {
        int a = 1;
        ;

        if (condition)
            ;
    }
}
```

### Output

After applying the recipe, standalone semicolons are removed and empty statements in control flow bodies are replaced with empty blocks.

```java
public class ExampleClass {
    public void example(boolean condition) {
        int a = 1;

        if (condition)
        {
        }
    }
}
```

### Diff

This diff highlights the exact changes made by OpenRewrite:

```diff
--- a/ExampleClass.java
+++ b/ExampleClass.java
@@ -2,5 +2,6 @@
     public void example(boolean condition) {
         int a = 1;
-        ;
 
         if (condition)
-            ;
+        {
+        }
     }
```
