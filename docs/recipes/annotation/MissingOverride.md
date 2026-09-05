# MissingOverride Recipe

The `MissingOverride` recipe fixes Checkstyle MissingOverride violations by adding the `@Override` annotation to methods that override a method from a superclass or implement a method from an interface (e.g., when the `{@inheritDoc}` Javadoc tag is present).

For more detailed information on this rule, please refer to the official [Checkstyle MissingOverride Documentation](https://checkstyle.org/checks/annotation/missingoverride.html).

## Usage

To configure this Checkstyle rule, add the following module to your Checkstyle configuration file (`checkstyle.xml`):

```xml
<module name="Checker">
  <module name="TreeWalker">
    <module name="MissingOverride"/>
  </module>
</module>
```

## Example

Below is an example showing how the `MissingOverride` recipe transforms your code.

### Input

The following method implements an interface method and includes the `{@inheritDoc}` Javadoc tag, but does not have the `@Override` annotation.

```java
public class ExampleClass implements Runnable {
    /**
     * {@inheritDoc}
     */
    public void run() {
    }
}
```

### Output

After applying the recipe, the `@Override` annotation is added to the method.

```java
public class ExampleClass implements Runnable {
    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
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
      * {@inheritDoc}
      */
+    @Override
     public void run() {
```
