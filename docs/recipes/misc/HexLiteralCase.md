# HexLiteralCase Recipe

The `HexLiteralCase` recipe fixes Checkstyle HexLiteralCase violations by replacing lowercase hexadecimal letters (`a-f`) with uppercase letters (`A-F`).

For more detailed information on this rule, please refer to the official [Checkstyle HexLiteralCase Documentation](https://checkstyle.org/checks/misc/hexliteralcase.html).

## Usage

To configure this Checkstyle rule, add the following module to your Checkstyle configuration file (`checkstyle.xml`):

```xml
<module name="Checker">
  <module name="TreeWalker">
    <module name="HexLiteralCase"/>
  </module>
</module>
```

## Example

Below is an example showing how the `HexLiteralCase` recipe transforms your code.

### Input

The following class contains hexadecimal literals using lowercase characters.

```java
public class ExampleClass {
    int hexInt = 0x1a;
    long hexLong = 0x7fff_ffff_ffff_ffffL;
    short hexShort = 0xf5f;
}
```

### Output

After applying the recipe, lowercase hexadecimal characters are converted to uppercase.

```java
public class ExampleClass {
    int hexInt = 0x1A;
    long hexLong = 0x7FFF_FFFF_FFFF_FFFFL;
    short hexShort = 0xF5F;
}
```

### Diff

This diff highlights the exact changes made by OpenRewrite:

```diff
--- a/ExampleClass.java
+++ b/ExampleClass.java
@@ -2,4 +2,4 @@
 public class ExampleClass {
-    int hexInt = 0x1a;
-    long hexLong = 0x7fff_ffff_ffff_ffffL;
-    short hexShort = 0xf5f;
+    int hexInt = 0x1A;
+    long hexLong = 0x7FFF_FFFF_FFFF_FFFFL;
+    short hexShort = 0xF5F;
 }
```
