# openapi-codegen
<a href="https://github.com/davidmoten/openapi-codegen/actions/workflows/ci.yml"><img src="https://github.com/davidmoten/openapi-codegen/actions/workflows/ci.yml/badge.svg"/></a><br/>
[![codecov](https://codecov.io/gh/davidmoten/openapi-codegen/branch/master/graph/badge.svg)](https://codecov.io/gh/davidmoten/openapi-codegen)<br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/au.gov.amsa/openapi-codegen/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/au.gov.amsa/openapi-codegen)<br/>

Generates server-side and client-side Java classes of OpenAPI v3.0.3 (3.1 support coming bit-by-bit) using Jackson for serialization/deserialization, server-side targets Spring Boot. Born out of frustrations with [*openapi-generator*](https://github.com/OpenAPITools/openapi-generator) and can be used standalone or in partnership with that project.

I suspect the future of this project will be to generate Java clients for APIs rather than server-side (except for one primary target that will be used for unit testing). The main reason for this is really the huge number of server-side frameworks that are out there. Yet to be decided!

Try it online [here](https://openapi-codegen.davidmoten.org/prod/site/index.html)!

**Features**
* Very clean minimal generated code (reused logic is in runtime libraries (server or client))
* Immutable generated schema classes (none of this mutable Java beans getters-and-setters rubbish)
* Extensively unit tested (and easy to add more to either demonstrate problems or correctness)
* Supports Java 8, 11, 17, 21 (CI)
* Supports Spring Boot 2.x, 3.x server-side
* Supports `oneOf`(discriminated/non-discriminated), `anyOf` (non-discriminated), `allOf`
* `oneOf` and `anyOf` validate on creation
* `allOf` generates an uber object with all members properties and `asBlah()` methods to access the individual members in a typed fashion
* Nesting in openapi definition reflected in nested Java classes
* Generates [chained builders](https://github.com/davidmoten/java-builder-pattern-tricks#trick-3-enforce-mandatory-fields-at-compile-time-with-builder-chaining) (chaining occurs when mandatory fields are present). This makes checking setting of mandatory fields a compile-time check.
* Strong typing (primitives are used for mandatory simple types, chained builders)
* Java 8 Optional and DateTime types used
* Generates `equals`, `hashCode`, `toString` methods
* Plenty of unit tests (good ones, full serialization and deserialization tests)
* Maven plugin
* Simple server-side and client-side implementation for primary and general response handling
* Partial use of schema generated classes possible with generated server and client of *openapi-generator-plugin*
* Constructor validation of schema objects means fail-fast which helps with diagnosis
* `multipart/form-data` request body support (client)
* `form-urlencoded` request body support (client)
* individual requests can be customized with timeouts and extra headers
* use Java HttpsURLConnection for HTTP interactions or use Apache Httpclient 5.x (raise an issue to add another Http library)

**Status**: released to Maven Central

## Limitations
* `allOf` only with object schemas
* parameter types like explode, label, deepObject not implemented yet
* multipart and form url encoded request bodied implemented on client, not server yet.
* security schemes not modelled (implement an `Interceptor` or use `BearerAuthenticator` or `BearerAuthenticator`)
* json only (xml not supported)

## Getting started

Working examples are at [openapi-codegen-example-pet-store](https://github.com/davidmoten/openapi-codegen/tree/master/openapi-codegen-example-pet-store) (client and server) and [openapi-codegen-example-pet-store](https://github.com/davidmoten/openapi-codegen/tree/master/openapi-codegen-example-pet-store-client) (client only).

Add this to your pom.xml in the `build/plugins` section:
```xml
<plugin>
    <groupId>com.github.davidmoten</groupId>
    <artifactId>openapi-codegen-maven-plugin</artifactId>
    <version>VERSION_HERE</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <basePackage>pet.store</basePackage>
            </configuration>
        </execution>
    </executions>
</plugin>
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <version>3.4.0</version>
    <executions>
        <execution>
            <id>add-source</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>add-source</goal>
            </goals>
            <configuration>
                <sources>
                    <source>${project.build.directory}/generated-sources/java</source>
                </sources>
            </configuration>
        </execution>
    </executions>
</plugin>
```
The example above generates java code from `*.yml, *.yaml` files in `src/main/openapi` directory.

We include *build-helper-maven-plugin* to help IDEs be aware that source generation is part of a Maven refresh in the IDE (for example in Eclipse **Maven - Update project** will run the codegen plugin and display the generated sources on the build path).  

Here's an example showing more configuration options:
```xml
<plugin>
    <groupId>com.github.davidmoten</groupId>
    <artifactId>openapi-codegen-maven-plugin</artifactId>
    <version>VERSION_HERE</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <basePackage>pet.store</basePackage>
                <outputDirectory>${project.build.directory}/generated-sources/java</outputDirectory>
                <sources>
                    <directory>${project.basedir}/src/main/openapi</directory>
                    <includes>
                        <include>**/*.yml</include>
                    </includes>
                </sources>
                <failOnParseErrors>false</failOnParseErrors>
                <includeSchemas>
                    <includeSchema>Thing</includeSchema>
                </includeSchemas>
                <excludeSchemas>
                    <excludeSchema>Error</excludeSchema>
                </excludeSchemas>
                <mapIntegerToBigInteger>false</mapIntegerToBigInteger>
                <generator>spring2</generator>
                <generateService>true</generateService>
                <generateClient>true</generateClient>
                <!-- attempts to limit class names by removing lower case vowels only -->
                <maxClassNameLength>100</maxClassNameLength>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## General advice
* Prefer yaml definitions to json, much easier to read and to hold comments
* As much as possible make sure you put your types in the `#/components/schemas` section of your openapi yaml/json file (use `$ref`!). The same goes for responses, pathItems, and anything else that can be referred to with a `$ref`. Don't use anonymous types, it makes for an ugly experience with generated code.
* avoid using passing complex parameters, use json request bodies instead (Spring Boot for example doesn't have annotation support for many parameter strategies)
* Specify `format: int32` on integers to ensure you end up with `int/integer` types in generated code
* Be sure to specify the properties that are mandatory (using `required:`)
* Set an `operationId` field for every path entry to ensure you get sensible generated method names (in client and server)
* always specify `mapping` and `propertyName` fields for discriminated `oneOf` (but prefer non-discriminated `oneOf`)
* use OpenAPI 3.0 not 3.1 (the world is still working on tool support for 3.0 and is not ready for 3.1)

## Generated code examples
Some examples follow. Note the following:

* really clean code, formatted, sensible whitespacing, no long code lines 
* minimal generated code (for example `toString`, `hashCode`, and oneOf Deserializer are one statement methods that pass off to non-generated runtime dependencies)
* type safety
* concise chained builders that check mandatory fields are set at compile-time
* constructor validation that can be configured off on a class by class basis
* Optional/JsonNullable should be used, not null values, in all public interactions  

### Schema classes
Note validations in constructors, private constructors for use with Jackson that wants nulls, public constructors that disallow nulls (use java.util.Optional), mandatory/optional fields, chained builder for maximal type-safety and readability,  immutable mutator methods, generated hashCode, equals, toString methods.

* [Book.java](src/docs/Book.java)
* [User.java](src/docs/User.java) 
* [Language.java](src/docs/Language.java) (enum)

### *oneOf* with discriminator
[Vehicle.java](src/docs/Vehicle.java), [Car.java](src/docs/Car.java), [Bike.java](src/docs/Bike.java)

Note that discriminators are constants that the user does not set (in fact, cannot set) and are set in the private constructors of Car and Bike.

### *oneOf* without discriminator
[Geometry.java](src/docs/Geometry.java), [Circle.java](src/docs/Circle.java), [Rectangle.java](src/docs/Rectangle.java)

### *anyOf* without discriminator
*anyOf* is an interesting one, mainly because it is rarely used appropriately. In a review of 21 apis in [openapi-directory], 5 had valid use-cases for *anyOf* and the rest should have been *oneOf*. Using *anyOf* instead of *oneOf* will still support *oneOf* semantics but generated code will not give you as clean an experience (type-safety wise) than if *oneOf* had been used explicitly.

[PetSearch.java](src/docs/PetSearch.java), [PetByAge.java](src/docs/PetByAge.java), [PetByType.java](src/docs/PetByType.java)

[AnyOfSerializer.java](openapi-codegen-runtime/src/main/java/org/davidmoten/oa3/codegen/runtime/AnyOfSerializer.java), [PolymorphicDeserializer.java](openapi-codegen-runtime/src/main/java/org/davidmoten/oa3/codegen/runtime/PolymorphicDeserializer.java)

### *allOf*
Uses composition but also exposes all subschema properties at allOf class level (that delegate to subschema objects).

[Dog3.java](src/docs/Dog3.java), [Cat3.java](src/docs/Cat3.java), [Pet3.java](src/docs/Pet3.java)

```yaml
    Pet3:
      type: object
      required:
        - petType
      properties:
        petType:
          type: string
    Dog3:
      allOf: # Combines the main `Pet3` schema with `Dog3`-specific properties
        - $ref: '#/components/schemas/Pet3'
        - type: object
          # all other properties specific to a `Dog3`
          properties:
            bark:
              type: boolean
            breed:
              type: string
              enum: [Dingo, Husky, Retriever, Shepherd]
    Cat3:
      allOf: # Combines the main `Pet` schema with `Cat`-specific properties
        - $ref: '#/components/schemas/Pet3'
        - type: object
          # all other properties specific to a `Cat3`
          properties:
            hunts:
```

### Generated client
Here's an example of the generated client class (the entry point for interactions with the API). Note the conciseness and reliance on type-safe builders from a non-generated dependency.

[Client.java](src/docs/Client.java)  

### Generated Spring server-side classes

* [Service.java](src/docs/Service.java)
* [ServiceController.java](src/docs/ServiceController.java) 

### Immutability
All generated classes are immutable though List and Map implementations are up to the user (you can use mutable java platform implementations or another library's immutable implementations).

To modify one field (or more) of a generated schema object, use the `with*` methods. But remember, these are immutable classes, you **must** assign the result. For example:

```java
Circle circle = Circle
    .latitude(Latitude.of(-10))
    .longitude(Longitude.of(140))
    .radiusNm(200)
    .build();
Circle circle2 = circle.withRadiusNm(250);
```

## Builders
All generated schema classes have useful static builder methods. Note that mandatory fields are modelled using chained builders so that you get compile-time confirmation 
that they have been set (and you don't need to set the optional fields). Public constructors are also available if you prefer.

Here's an example (creating an instance of `Geometry` which was defined as `oneOf`:
```java
Geometry g = Geometry.of(Circle
    .builder() 
    .lat(Latitude.of(-35f))
    .lon(Longitude.of(142f))
    .radiusNm(20)
    .build());
```
Note that if the first field is mandatory you can omit the `builder()` method call:
```java
Geometry g = Geometry.of(Circle
    .lat(Latitude.of(-35f))
    .lon(Longitude.of(142f))
    .radiusNm(20)
    .build());
```

## Validation
Enabled/disabled by setting a new `Globals.config`. Configurable on a class-by-class basis.

## Nulls
The classes generated by *openapi-codegen* **do not allow null parameters** in public method

OpenAPI v3 allows the specification of fields with `nullable` set to `true`. When `nullable` is true for a property (like `thing`)
then the following fragments must be distinguishable in serialization and deserialization:

```json
{ "thing" : null }
```
and 
```json
{}
```
This is achieved using the special class `JsonNullable` from [OpenAPITools](https://github.com/OpenAPITools/jackson-databind-nullable). When you want an entry like `"thing" : null` to be
preserved in json then pass `JsonNullable.of(null)`. If you want the entry to be absent then pass `JsonNullable.undefined`.

For situations where `nullable` is false (the default) then pass `java.util.Optional`. The API itself will make this obvious.

## Logging
`slf4j` is used for logging. Add the implementation of your choice. 

## Client
The generated client is used like so:

```java
BearerAuthenticator authenticator = () -> "tokenthingy";
Client client = Client
     .basePath("https://myservice.com/api")
     .interceptor(authenticator)
     .build();

// make calls to the service methods:
Thing thing = client.thingGet("abc123");
```

### Interceptors
Interceptors are specified in a client builder and allow the modification (method, url, headers) of all requests. An obvious application for an interceptor is authentication where you can 
add a Bearer token to every request. 

### Authentication
Set an interceptor in the client builder to an instance of `BearerAuthenticator` or `BasicAuthenticator` or do your own thing entirely.

### HttpService
The [HttpService](https://github.com/davidmoten/openapi-codegen/blob/master/openapi-codegen-http/src/main/java/org/davidmoten/oa3/codegen/http/service/HttpService.java) can be set in the Client builder and encapsulates all HTTP interactions. The default HttpService is `DefaultHttpService.INSTANCE` which is based on HttpURLConnection class. Funnily enough the java HttpURLConnection classes don't support the HTTP PATCH verb. The default HttpService makes PATCH calls as POST calls with the header `X-HTTP-Method-Override: PATCH` which is understood by most web servers. If you'd like to use the PATCH verb then call `.allowPatch()` on the Client builder (for instance if you've modified HttpURLConnection static field using reflection to support PATCH).

The alternative to the default HttpService is `ApacheHttpClientHttpService.INSTANCE` which is based on Apache Httpclient 5.x (and has full support for the PATCH verb).

## Multipart requests
Client code is generated for multipart/form-data requests specified in the openapi definition, including setting custom content types per part. Here's an example:

OpenAPI fragment:
```yaml
paths:
  /upload:
    post:
      requestBody:
        content:
          multipart/form-data:
            schema:
              type: object
              properties:
                point:
                  $ref: '#/components/schemas/Point'
                description:
                  type: string
                document:
                  type: string
                  format: binary  
              required: [point, description, document]
            encoding:
              document:
                contentType: application/pdf
      responses:
        200:
          description: ok
          content:
            application/json: {}
```
Below is the generated type for the multipart/form-data submission object. 

* [UploadPostRequestMultipartFormData.java](src/docs/UploadPostRequestMultipartFormData.java)

Here's the client code that uses it:
```java
UploadPostRequestMultipartFormData upload = UploadPostRequestMultipartFormData
  .point(Point.lat(-23).lon(135).build()) 
  .description("theDescription") 
  .document(Document 
     .contentType(ContentType.APPLICATION_PDF) 
     .value(new byte[] { 1, 2, 3 }) 
     .build()) 
  .build();
client.uploadPost(upload);
```

## readOnly and writeOnly
Sometimes you want to indicate that parts of an object are used only in a response or only in a request (but the core parts of the object might be used in both). That's where `readOnly` and `writeOnly` keywords come in.

If a field is marked `readOnly`
* it should not be transmitted over the wire in a request
* the object containing the field should still be constructable without that field so that the object can be used in a request

If a field is marked `writeOnly`
* it should not be transmitted over the wire in a response (and if it is then that field should be empty)
* the object containing the field should still be constructable without that field so that the object can be used in a response

Marking a property as `readOnly` has the following effects on generated code:
* regardless of whether the property is required or not the field will be typed as `Optional`
* if the property is required then 
  * the constructor will allow `Optional.empty` to be passed 
  * a custom deserializer will be used to fail if `Optional.empty` (null or absent) is passed 
* the object can be built using the builder or the constructor with or without the `readOnly` field (
it is only at deserialization time that we enforce a required property)

Here's an example of generated code with `readOnly` fields: [ReadOnly.java](src/docs/ReadOnly.java).

Marking a property as `writeOnly` has the following effects on generated code:
* regardless of whether the property is required or not the field will be typed as `Optional`
* if the property is required then 
  * the constructor will allow `Optional.empty` to be passed 
  * a custom serializer will be used to fail if `Optional.empty` (null or absent) is passed 
* the object can be built using the builder or the constructor with or without the `writeOnly` field (
it is only at serialization time that we enforce a required property)

Here's an example of generated code with `writeOnly` fields: [WriteOnly.java](src/docs/WriteOnly.java).

## Server side generation
### Ignoring paths for server side generation
Just add an extension to the OpenAPI file to indicate to the generator not to generate a server side method for a path:

```yaml
paths:
  /upload:
    post:
      x-openapi-codegen-include-for-server-generation: false
      ...
```

An example of supplementing generated spring server with an HttpServlet is in these classes:
* [PathsApplication.java](https://github.com/davidmoten/openapi-codegen/blob/master/openapi-codegen-maven-plugin-test/src/test/java/org/davidmoten/oa3/codegen/test/paths/PathsApplication.java)
* [FormServlet.java](https://github.com/davidmoten/openapi-codegen/blob/master/openapi-codegen-maven-plugin-test/src/test/java/org/davidmoten/oa3/codegen/test/paths/FormServlet.java)
* [MultipartServlet.java](https://github.com/davidmoten/openapi-codegen/blob/master/openapi-codegen-maven-plugin-test/src/test/java/org/davidmoten/oa3/codegen/test/paths/MultipartServlet.java)

## Mixed usage with *openapi-generator*
See [this](https://github.com/davidmoten/openapi-codegen/wiki/openapi-generator#mixed-usage-with-openapi-generator).

## What about openapi-generator project?
This project *openapi-codegen* is born out of the insufficiences of [openapi-generator](https://github.com/OpenAPITools/openapi-generator). Great work by that team but VERY ambitious. That team is up against it, 37 target languages, 46 server frameworks, 200K lines of java code, 30K lines of templates. April 2023 there were 3,500 open issues (whew!).

So what's missing and what can we do about it? Quite understandably there is a simplified approach in *openapi-generator* code to minimize the work across many languages with varying capabilities. For Java this means a lot of hassles:
* Mutable classes mean that validation cannot be performed at construction time and have to use validation-api annotations. Errors raised at serialization time not at object creation time so finding the cause of the error is problematic.
* Missing out on the many benefits of immutability (google for benefits of immutability)
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

## Null safety
The choice has been made with *openapi-generator* that nulls should never be passed as parameters in methods or constructors (and empty is represented by `Optional.empty()` or by not specifying a field in a builder method. 

Java docs written at the release of Java 8 in 2014 state that

>Optional is primarily intended for use as a method return type where there is a clear need to represent "no result," and where using null is likely to cause errors.

When is null likely to cause errors? If the consumer of the method is a member of a small team that knows a domain well perhaps the answer is not often. The answer is *anytime* if the consumer of the method is an arbitrary member of the public, and that is the use case being supported by *openapi-codegen*.

Though interesting to know the intent, the statement carries little argumentation so is not the basis for a decision about more widespread use.

As a Scala user I came distinctly aware of the power of using `Option` everywhere instead of `null`. There are no end of conversations out there on the web about the evils of `null`, the case is strong for the cost of unexpected `NullPointerException`s.

As a Java user I've always been frustrated having to dive into javadoc to decide if a parameter is nullable. Life is easier if it's written into the signature of the parameter, and I don't mind wrapping the odd parameter with `Optional.ofNullable` (for those parameters that aren't constant, dynamically determined).

HTTP API interaction is a fundamentally IO limited activity (though networks are getting faster and localhost networking is well ahead of interhost networking). As such the use of Optional wrappers here and there is not the performance consideration that it might be if GC pressure (from object creation) was a limiting factor.

Builder use means that unwrapped non-null values can always be passed as parameters.

IntelliJ IDEA detects and warns about use of Optional types in fields and method parameters. That's an unnecessary warning, I'd suppress it.

## Testing
Lots of unit tests happening, always room for more.

Most of the code generation tests happen in *openapi-codegen-maven-plugin-test* module. Path related stuff goes into `src/main/openapi/paths.yml` and schema related stuff goes in to `src/main/openapi/main.yml`. Unit tests of generated classes form those yaml files are in `src/test/java`.

In addition to unit tests, openapi-codegen appears to generate valid classes for the following apis:
* EBay
* Marqueta
* OpenFlow
* Spotify
* Google Chat
* Federal Electoral Commission
* BitBucket
* MailChimp
* GitHub
* OpenAI
* Atlassian JIRA
* Twitter
* Stripe

Docusign api needs work here because has more than 255 fields in an object which exceeds Java constructor limits.

To run tests on the above apis call this:
```bash
./test-all.sh 
```
This script ensures that the code generated from the above large test apis compiles and does so in many separate generation and compile steps because the apis generate so much code that the compilation step runs out of memory on my devices!

### openapi-directory testing
If *openapi-directory* repository is cloned next to *openapi-codegen* in your workspace then the command below will test code generation on every 3.0 definition (>1800) in that repository. This command requires `mvnd` to be [installed](https://github.com/apache/maven-mvnd).

```bash
cd openapi-codegen-generator
./analyse.sh 
```
Output is written to `~/oc-TIMESTAMP.log`

For convenience I add executables to `/usr/local/bin` with `./install-executables.sh`. That way I can run `codegen` or `codegenc` from anywhere.

## TODO
* ~~`additionalProperties` (Dictionary) support~~, done
* generate javadoc for fields
* `not` support
* `anyOf` with discriminator support
* delegate constructors using `this(`
* workaround JsonCreator not being able to pass `5` into a double argument, must be `5.0` (https://github.com/FasterXML/jackson-core/issues/532)
* document limited support for parameter style with spring-boot rest
* support objects with more than 255 fields (max parameter number in Java gets exceeded in object constructor)
* support form-style request bodies on server-side (multipart, urlencoded). Client side support done (not comprehensive).
* support more parameter styles
* support xml
* write docs
* support 3.1 features (type arrays, null instead of nullable, contentMediaType and contentEncoding for file payloads)
