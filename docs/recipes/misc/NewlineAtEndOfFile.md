# NewlineAtEndOfFile Recipe

The `NewlineAtEndOfFile` recipe fixes Checkstyle NewlineAtEndOfFile violations by ensuring that Java source files end with a newline character.

For more detailed information on this rule, please refer to the official [Checkstyle NewlineAtEndOfFile Documentation](https://checkstyle.org/checks/misc/newlineatendoffile.html).

## Usage

To configure this Checkstyle rule, add the following module to your Checkstyle configuration file (`checkstyle.xml`):

```xml
<module name="Checker">
  <module name="NewlineAtEndOfFile"/>
</module>
```

### Options

| Property | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `lineSeparator` | `string` | `"system"` | The type of line separator to use (`system`, `lf`, `crlf`, `cr`, `lf_cr_crlf`). |
| `fileExtensions` | `String[]` | `""` | File extensions to check. |

```xml
<module name="Checker">
  <module name="NewlineAtEndOfFile">
    <property name="lineSeparator" value="lf"/>
  </module>
</module>
```

## Example

Below is an example showing how the `NewlineAtEndOfFile` recipe transforms your code.

### Input

The following file ends abruptly without a trailing newline character.

```java
public class ExampleClass {
}
```

### Output

After applying the recipe, a newline character is appended to the end of the file.

```java
public class ExampleClass {
}

```

### Diff

This diff highlights the exact changes made by OpenRewrite:

```diff
--- a/ExampleClass.java
+++ b/ExampleClass.java
@@ -1,2 +1,2 @@
 public class ExampleClass {
-}
\ No newline at end of file
+}
```
