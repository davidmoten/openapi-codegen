# openapi-codegen
<a href="https://github.com/davidmoten/openapi-codegen/actions/workflows/ci.yml"><img src="https://github.com/davidmoten/openapi-codegen/actions/workflows/ci.yml/badge.svg"/></a><br/>
[![codecov](https://codecov.io/gh/davidmoten/openapi-codegen/branch/master/graph/badge.svg)](https://codecov.io/gh/davidmoten/openapi-codegen)<br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/au.gov.amsa/openapi-codegen/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/au.gov.amsa/openapi-codegen)<br/>

Jackson annotated Java 8+ code generator (via a maven plugin) from all Schema sections of an OpenAPI 3.0.1 definition file. Can be used with [openapi-generator](https://github.com/OpenAPITools/openapi-generator) to fill in the functionality gaps of that project.

**Status**: in development 

This project is born out of the insufficiences of [openapi-generator](https://github.com/OpenAPITools/openapi-generator). Great work by that team but VERY ambitious. That team is up against it, 37 target languages, 46 server frameworks, 200K lines of java code, 30K lines of templates. April 2023 there were 3,500 open issues (whew!).

So what's missing and what can we do about it? Quite understandably there is a simplified approach in *openapi-generator* code to minimize the work across many languages with varying capabilities. For Java this means a lot of hassles:
* Mutable classes mean that validation cannot be performed at construction time and have to use validation-api annotations
* Mutable classes not good 
* No support for oneOf, anyOf when no discriminator specified
* when discriminator mappings specified two sets of conflicting mapping annotations are generated
* SimpleRef case has no type safety (Ref is passed in as Object in constructor)
* unnecessary generated imports
* anonymous schemas generated as top level classes when could be nested static member classes (pollutes top level package)
* should be able to create oneOf member without specifying discriminator value in constructor (is constant)
* field types should be primitives in constructors, getters when mandatory (means a compile-time error instead of a runtime error) 
* testing approach in the project lacks JSON serialization and deserialization tests at a unit level (as opposed to starting up servers and doing integration tests)

Here's what's good about this project:
* very clean generated code
* Lots of type safety
* Immutable generated classes
* oneOf, anyOf, allOf support (MUCH better than openapi-generator at the moment)
* Java 8+ date/time classes
* `java.util.Optional` used in generated classes to make clear what is required
* JSON serialization and deserialization unit tested thoroughly (and easy to add more)
* can be combined with openapi-generator-maven-plugin using [Bring your own models](https://openapi-generator.tech/docs/customization/#bringing-your-own-models)

## TODO
* `additionalProperties` (Dictionary) support
* generate javadoc for fields
* `not` support
* delegate constructors using `this(`
* add equals and hashCode
* workaround JsonCreator not being able to pass `5` into a double argument, must be `5.0` (https://github.com/FasterXML/jackson-core/issues/532)
* support external schema refs
* write docs
