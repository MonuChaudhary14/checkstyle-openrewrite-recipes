# MissingDeprecated Recipe

The `MissingDeprecated` recipe fixes Checkstyle MissingDeprecated violations by ensuring that both the `@Deprecated` annotation and the `@deprecated` Javadoc tag are present whenever either one is used.

For more detailed information on this rule, please refer to the official [Checkstyle MissingDeprecated Documentation](https://checkstyle.org/checks/annotation/missingdeprecated.html).

## Usage

To configure this Checkstyle rule, add the following module to your Checkstyle configuration file (`checkstyle.xml`):

```xml
<module name="Checker">
  <module name="TreeWalker">
    <module name="MissingDeprecated"/>
  </module>
</module>
```

## Example

Below is an example showing how the `MissingDeprecated` recipe transforms your code.

### Input

The following method contains a `@deprecated` Javadoc tag but lacks the corresponding `@Deprecated` Java annotation.

```java
public class ExampleClass {
    /**
     * @deprecated Use newMethod() instead.
     */
    public void oldMethod() {
    }
}
```

### Output

After applying the recipe, the missing `@Deprecated` annotation is added.

```java
public class ExampleClass {
    /**
     * @deprecated Use newMethod() instead.
     */
    @Deprecated
    public void oldMethod() {
    }
}
```

### Diff

This diff highlights the exact changes made by OpenRewrite:

```diff
--- a/ExampleClass.java
+++ b/ExampleClass.java
@@ -3,3 +3,4 @@
     /**
      * @deprecated Use newMethod() instead.
      */
+    @Deprecated
     public void oldMethod() {
```
