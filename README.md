# openapi-generator
Java code generator from OpenAPI definition file

## TODO
* array wrapper classes can be passed null in constructor?
* `additionalProperties` (Dictionary) support
* `allOf` support
* generate javadoc for fields
* delegate constructors using `this(`
* use jakarta validation jar
* add equals and hashCode

## Notes on OpenAPI openapi-generator

* Mutable classes mean that validation cannot be performed at construction time and have to use validation-api annotations
* Mutable classes bad
* No support for oneOf, anyOf when no discriminator specified
* when discriminator mappings specified two sets of conflicting mapping annotations are generated
* SimpleRef case has no type safety (Ref is passed in as Object in constructor)
* unnecessary generated imports
* anonymous schemas generated as top level classes when could be nested static member classes (pollutes top level package)
* should be able to create oneOf member without specifying discriminator value in constructor (is constant)
* field types should be primitives in constructors, getters when mandatory (means a compile-time error instead of a runtime error) 
