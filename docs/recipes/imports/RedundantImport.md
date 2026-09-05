# RedundantImport Recipe

The `RedundantImport` recipe fixes Checkstyle RedundantImport violations by removing duplicate import statements, imports from the `java.lang` package, and imports from the current package.

For more detailed information on this rule, please refer to the official [Checkstyle RedundantImport Documentation](https://checkstyle.org/checks/imports/redundantimport.html).

## Usage

To configure this Checkstyle rule, add the following module to your Checkstyle configuration file (`checkstyle.xml`):

```xml
<module name="Checker">
  <module name="TreeWalker">
    <module name="RedundantImport"/>
  </module>
</module>
```

## Example

Below is an example showing how the `RedundantImport` recipe transforms your code.

### Input

The following class contains duplicate and redundant `java.lang` imports.

```java
package com.example;

import java.util.List;
import java.util.List;
import java.lang.String;

public class ExampleClass {
    List<String> items;
}
```

### Output

After applying the recipe, redundant import statements are removed.

```java
package com.example;

import java.util.List;

public class ExampleClass {
    List<String> items;
}
```

### Diff

This diff highlights the exact changes made by OpenRewrite:

```diff
--- a/ExampleClass.java
+++ b/ExampleClass.java
@@ -3,3 +3,1 @@
 import java.util.List;
-import java.util.List;
-import java.lang.String;
```
