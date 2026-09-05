# NumericalPrefixesInfixesSuffixesCharacterCase Recipe

The `NumericalPrefixesInfixesSuffixesCharacterCase` recipe fixes Checkstyle NumericalPrefixesInfixesSuffixesCharacterCase violations by replacing uppercase prefixes (`0X`, `0B`), infixes (`E`, `P`), and suffixes (`F`, `D`) with lowercase characters.

For more detailed information on this rule, please refer to the official [Checkstyle NumericalPrefixesInfixesSuffixesCharacterCase Documentation](https://checkstyle.org/checks/misc/numericalprefixesinfixessuffixescharactercase.html).

## Usage

To configure this Checkstyle rule, add the following module to your Checkstyle configuration file (`checkstyle.xml`):

```xml
<module name="Checker">
  <module name="TreeWalker">
    <module name="NumericalPrefixesInfixesSuffixesCharacterCase"/>
  </module>
</module>
```

## Example

Below is an example showing how the `NumericalPrefixesInfixesSuffixesCharacterCase` recipe transforms your code.

### Input

The following class uses uppercase characters for numerical prefixes, infixes, and suffixes.

```java
public class ExampleClass {
    int hex = 0X1A;
    int bin = 0B1010;
    float exp = 1.23E3F;
    double hexExp = 0x1.3P2D;
}
```

### Output

After applying the recipe, prefixes, infixes, and suffixes are converted to lowercase.

```java
public class ExampleClass {
    int hex = 0x1A;
    int bin = 0b1010;
    float exp = 1.23e3f;
    double hexExp = 0x1.3p2d;
}
```

### Diff

This diff highlights the exact changes made by OpenRewrite:

```diff
--- a/ExampleClass.java
+++ b/ExampleClass.java
@@ -2,5 +2,5 @@
 public class ExampleClass {
-    int hex = 0X1A;
-    int bin = 0B1010;
-    float exp = 1.23E3F;
-    double hexExp = 0x1.3P2D;
+    int hex = 0x1A;
+    int bin = 0b1010;
+    float exp = 1.23e3f;
+    double hexExp = 0x1.3p2d;
 }
```
