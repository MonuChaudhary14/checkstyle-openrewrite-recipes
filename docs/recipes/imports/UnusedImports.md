# UnusedImports Recipe

The `UnusedImports` recipe fixes Checkstyle UnusedImports violations by removing import statements for types, methods, or fields that are never referenced in the source file.

For more detailed information on this rule, please refer to the official [Checkstyle UnusedImports Documentation](https://checkstyle.org/checks/imports/unusedimports.html).

## Usage

To configure this Checkstyle rule, add the following module to your Checkstyle configuration file (`checkstyle.xml`):

```xml
<module name="Checker">
  <module name="TreeWalker">
    <module name="UnusedImports"/>
  </module>
</module>
```

## Example

Below is an example showing how the `UnusedImports` recipe transforms your code.

### Input

The following class imports `java.util.Map`, which is not used anywhere in the file.

```java
package com.example;

import java.util.List;
import java.util.Map;

public class ExampleClass {
    List<String> list;
}
```

### Output

After applying the recipe, the unused import is removed.

```java
package com.example;

import java.util.List;

public class ExampleClass {
    List<String> list;
}
```

### Diff

This diff highlights the exact changes made by OpenRewrite:

```diff
--- a/ExampleClass.java
+++ b/ExampleClass.java
@@ -4,1 +4,0 @@
-import java.util.Map;
```
