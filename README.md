# openapi-codegen
<a href="https://github.com/davidmoten/openapi-codegen/actions/workflows/ci.yml"><img src="https://github.com/davidmoten/openapi-codegen/actions/workflows/ci.yml/badge.svg"/></a><br/>
[![codecov](https://codecov.io/gh/davidmoten/openapi-codegen/branch/master/graph/badge.svg)](https://codecov.io/gh/davidmoten/openapi-codegen)<br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/au.gov.amsa/openapi-codegen/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/au.gov.amsa/openapi-codegen)<br/>

Generates server-side and client-side Java classes of OpenAPI v3.0.3 using Jackson for serialization/deserialization, server-side targets Spring Boot. Born out of frustrations with [*openapi-generator*](https://github.com/OpenAPITools/openapi-generator) and can be used standalone or in partnership with that project.

**Features**
* Very clean minimal generated code (reused logic is in runtime libraries (server or client))
* Immutable generated schema classes (none of this mutable Java beans getters-and-setters rubbish)
* Extensively unit tested (and easy to add more to either demonstrate problems or correctness)
* Supports Java 8, 11, 17 (CI)
* Supports Spring Boot 2.x, 3.x server-side
* Supports polymorphism `oneOf`, `anyOf`, `allOf` with or without discriminators
* Nesting in openapi definition reflected in nested Java classes
* Strong typing (primitives are used for mandatory simple types)
* Java 8 Optional and DateTime types used
* Generates `equals`, `hashCode`, `toString` methods
* Generates chained builders (chaining occurs when mandatory fields are present)
* Plenty of unit tests (good ones, full serialization and deserialization tests)
* Maven plugin
* Simple server-side and client-side implementation for primary and general response handling
* Partial use of schema generated classes possible with generated server and client of *openapi-generator-plugin*
* Constructor validation of schema objects means fail-fast which helps with diagnosis

**Status**: in development, alpha release on Maven Central

## Limitations
* `allOf` only with object schemas
* parameter types like explode, label, deepObject not implemented yet
* json only (xml not supported)

## Usage
As much as possible make sure you put your types in the components/schemas section of your openapi yaml/json file (use $ref!). Don't use anonymous types, it makes for an ugly experience with generated code. 

## Generated code examples
Some examples follow. Note the following:

* really clean code, formatted, sensible whitespacing, no long code lines 
* minimal generated code (for example toString, hashCode, and oneOf Deserializer are one statement methods that pass off to non-generated runtime dependencies)
* type safety
* concise builders
* constructor validation that can be configured off on a class by class basis
* Optional should be used, not null values, in all public interactions  

### *oneOf* with discriminator
[Vehicle.java](src/docs/Vehicle.java), [Car.java](src/docs/Car.java), [Bike.java](src/docs/Bike.java)

Note that discriminators are constants that the user does not set (in fact, cannot set) and are set in the private constructors of Car and Bike.

### *oneOf* without discriminator
[Geometry.java](src/docs/Geometry.java), [Circle.java](src/docs/Circle.java), [Rectangle.java](src/docs/Rectangle.java)

## Validation

## Logging

## Interceptors

## Authentication

## HTTP Patch 

### Mixed usage with *openapi-generator*
See [this](https://github.com/davidmoten/openapi-codegen/wiki/openapi-generator#mixed-usage-with-openapi-generator).

## What about openapi-generator project?
This project *openapi-codegen* is born out of the insufficiences of [openapi-generator](https://github.com/OpenAPITools/openapi-generator). Great work by that team but VERY ambitious. That team is up against it, 37 target languages, 46 server frameworks, 200K lines of java code, 30K lines of templates. April 2023 there were 3,500 open issues (whew!).

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
* *import mapping* is very poor, doesn't handle related objects and doesn't update service classes (non-model classes)
* a LOT of bugs (3,500 open issues is an indicator)

## TODO
* `additionalProperties` (Dictionary) support
* generate javadoc for fields
* `not` support
* delegate constructors using `this(`
* workaround JsonCreator not being able to pass `5` into a double argument, must be `5.0` (https://github.com/FasterXML/jackson-core/issues/532)
* document limited support for parameter style with spring-boot rest
* write docs
