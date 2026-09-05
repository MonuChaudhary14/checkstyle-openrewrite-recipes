# MissingSwitchDefault Recipe

The `MissingSwitchDefault` recipe fixes Checkstyle MissingSwitchDefault violations by ensuring that every `switch` statement contains a `default` clause.

For more detailed information on this rule, please refer to the official [Checkstyle MissingSwitchDefault Documentation](https://checkstyle.org/checks/coding/missingswitchdefault.html).

## Usage

To configure this Checkstyle rule, add the following module to your Checkstyle configuration file (`checkstyle.xml`):

```xml
<module name="Checker">
  <module name="TreeWalker">
    <module name="MissingSwitchDefault"/>
  </module>
</module>
```

## Example

Below is an example showing how the `MissingSwitchDefault` recipe transforms your code.

### Input

The following `switch` statement does not include a `default` label.

```java
public class ExampleClass {
    public void process(int status) {
        switch (status) {
            case 1:
                System.out.println("Active");
                break;
        }
    }
}
```

### Output

After applying the recipe, a `default` clause is added to the `switch` statement.

```java
public class ExampleClass {
    public void process(int status) {
        switch (status) {
            case 1:
                System.out.println("Active");
                break;
            default:
                break;
        }
    }
}
```

### Diff

This diff highlights the exact changes made by OpenRewrite:

```diff
--- a/ExampleClass.java
+++ b/ExampleClass.java
@@ -5,3 +5,5 @@
             case 1:
                 System.out.println("Active");
                 break;
+            default:
+                break;
         }
```
