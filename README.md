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
* import mapping is very poor, doesn't handle related objects and doesn't update service classes (non-model classes)
* a LOT of bugs (3,500 issues is an indicator)

Here's what's good about this project:
* very clean generated code
* Lots of type safety
* Immutable generated classes
* oneOf, anyOf, allOf support (MUCH better than openapi-generator at the moment)
* Java 8+ date/time classes
* `java.util.Optional` used in generated classes to make clear what is required
* JSON serialization and deserialization unit tested thoroughly (and easy to add more)

## Usage

Unfortunately the import mappings configuration of *openapi-generator-plugin* 6.4.0 does not work well at all except for simple single class bandaids. I got cooperation between *openapi-generator* and *openapi-codegen* working by following this process:

* generate with openapi-generator-plugin to packages `my.company.server` and `my.company.model`
* use maven-antrun-plugin to 
  * replace all references to `my.company.model` to `my.company.alt.model` in `my/company/server/.*.java`
  * delete classes in `my/company/model`
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
                    <replace dir="${project.build.directory}/generated-sources/openapi/src/main/java" token="MsiGet200Response" value="Path_msi_Get_200" failOnNoReplacements="true">
                        <include name="**/api/*.java" />
                    </replace>
                    <replace dir="${project.build.directory}/generated-sources/openapi/src/main/java" token="my.company.model" value="my.company.alt.model" failOnNoReplacements="true">
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
    <version>${openapi.codegen.version}</version>
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

## TODO
* `additionalProperties` (Dictionary) support
* generate javadoc for fields
* `not` support
* delegate constructors using `this(`
* add equals and hashCode
* workaround JsonCreator not being able to pass `5` into a double argument, must be `5.0` (https://github.com/FasterXML/jackson-core/issues/532)
* support external schema refs
* write docs
