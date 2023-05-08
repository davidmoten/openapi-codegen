# openapi-codegen
<a href="https://github.com/davidmoten/openapi-codegen/actions/workflows/ci.yml"><img src="https://github.com/davidmoten/openapi-codegen/actions/workflows/ci.yml/badge.svg"/></a><br/>
[![codecov](https://codecov.io/gh/davidmoten/openapi-codegen/branch/master/graph/badge.svg)](https://codecov.io/gh/davidmoten/openapi-codegen)<br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/au.gov.amsa/openapi-codegen/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/au.gov.amsa/openapi-codegen)<br/>

Generates server-side and client-side Java classes of OpenAPI v3.0.3 using Jackson for serialization/deserialization, server-side targets Spring Boot. Born out of frustrations with [*openapi-generator*](https://github.com/OpenAPITools/openapi-generator) and can be used standalone or in partnership with that project.

Features
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

## Usage
As much as possible make sure you put your types in the components/schemas section of your openapi yaml/json file (use $ref!). Don't use anonymous types, it makes for an ugly experience with generated code. 

TODO

### Mixed usage with *openapi-generator*
Let's look at the case where we want to generate the server side (spring-boot) with openapi-generator but use the Jackson annotated schema classes generated by openapi-codegen (which support more of the OpenAPI standard).

Unfortunately the import mappings configuration of *openapi-generator-plugin* 6.4.0 does not work well at all except for simple single class bandaids. I got cooperation between *openapi-generator* and *openapi-codegen* working by following this process:

* generate with openapi-generator-plugin to packages `my.company.api` and `my.company.model`
* use maven-antrun-plugin to 
  * replace all references to `my.company.model` to `my.company.alt.model` in `my/company/server/.*.java`
  * delete `*.java` files in `my/company/model`
* generate with openapi-codegen-plugin to base package `my.company.alt` (which creates `my.company.alt.model` classes)

Here are the plugins doing the above (example):
```xml
<plugin>
    <groupId>org.openapitools</groupId>
    <artifactId>openapi-generator-maven-plugin</artifactId>
    <version>6.4.0</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <inputSpec>${project.basedir}/src/main/resources/api.yml</inputSpec>
                <generatorName>spring</generatorName>
                <apiPackage>my.company.api</apiPackage>
                <modelPackage>my.company.model</modelPackage>
                <configOptions>
                    <delegatePattern>true</delegatePattern>
                </configOptions>
            </configuration>
        </execution>
    </executions>
</plugin>
<plugin>
    <artifactId>maven-antrun-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <execution>
            <phase>generate-sources</phase>
            <configuration>
                <target>
                    <replace dir="${project.build.directory}/generated-sources/openapi/src/main/java" token="model.MsiGet200Response" value="path.MsiGet200Response" failOnNoReplacements="true">
                        <include name="**/api/*.java" />
                    </replace>
                    <replace dir="${project.build.directory}/generated-sources/openapi/src/main/java" token="my.company.model" value="my.company.alt.schema" failOnNoReplacements="true">
                        <include name="my/company/api/*.java" />
                        <include name="org/openapitools/**/*.java" />
                    </replace>
                    <delete>
                        <fileset dir="${project.build.directory}/generated-sources/openapi/src/main/java" includes="my/company/model/*.java" />
                    </delete>
                </target>
            </configuration>
            <goals>
                <goal>run</goal>
            </goals>
        </execution>
    </executions>
</plugin>
<plugin>
    <groupId>com.github.davidmoten</groupId>
    <artifactId>openapi-codegen-maven-plugin</artifactId>
    <version>VERSION_HERE</version>
    <executions>
        <execution>
            <id>generate-more</id>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <basePackage>my.company.alt</basePackage>
                <outputDirectory>${project.build.directory}/generated-sources/openapi/src/main/java</outputDirectory>
                <sources>
                    <directory>${project.basedir}/src/main/resources</directory>
                    <includes>
                        <include>api.yml</include>
                    </includes>
                </sources>
            </configuration>
        </execution>
    </executions>
</plugin>
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <version>3.3.0</version>
    <executions>
        <execution>
            <id>add-source</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>add-source</goal>
            </goals>
            <configuration>
                <sources>
                    <source>${project.build.directory}/generated-sources/openapi/src/main/java</source>
                </sources>
            </configuration>
        </execution>
    </executions>
</plugin>

```

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
