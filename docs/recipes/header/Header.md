# Header Recipe

The `Header` recipe fixes Checkstyle Header violations by adding the specified header or license comment to Java source files when it is missing.

For more detailed information on this rule, please refer to the official [Checkstyle Header Documentation](https://checkstyle.org/checks/header/header.html).

## Usage

To configure this Checkstyle rule, add the following module to your Checkstyle configuration file (`checkstyle.xml`):

```xml
<module name="Checker">
  <module name="Header">
    <property name="headerFile" value="config/java.header"/>
  </module>
</module>
```

### Options

| Property | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `headerFile` | `string` | `null` | Path to the file containing the required header. |
| `header` | `string` | `null` | Header string to be used directly. |
| `charset` | `string` | System default | Character set used when reading the header file. |

```xml
<module name="Checker">
  <module name="Header">
    <property name="header" value="// Copyright 2026\n// All rights reserved."/>
  </module>
</module>
```

## Example

Below is an example showing how the `Header` recipe transforms your code.

### Input

The following Java source file does not have the required header comment at the top.

```java
package com.example;

public class ExampleClass {
}
```

### Output

After applying the recipe, the configured header is added at the beginning of the file.

```java
// Copyright 2026
package com.example;

public class ExampleClass {
}
```

### Diff

This diff highlights the exact changes made by OpenRewrite:

```diff
--- a/ExampleClass.java
+++ b/ExampleClass.java
@@ -1,3 +1,4 @@
+// Copyright 2026
 package com.example;
 
 public class ExampleClass {
```
