# openapi-codegen
<a href="https://github.com/davidmoten/openapi-codegen/actions/workflows/ci.yml"><img src="https://github.com/davidmoten/openapi-codegen/actions/workflows/ci.yml/badge.svg"/></a><br/>
[![codecov](https://codecov.io/gh/davidmoten/openapi-codegen/branch/master/graph/badge.svg)](https://codecov.io/gh/davidmoten/openapi-codegen)<br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/au.gov.amsa/openapi-codegen/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/au.gov.amsa/openapi-codegen)<br/>

Generates server-side and client-side Java classes of OpenAPI v3.0.3 using Jackson for serialization/deserialization, server-side targets Spring Boot. Born out of frustrations with [*openapi-generator*](https://github.com/OpenAPITools/openapi-generator) and can be used standalone or in partnership with that project.

I suspect the future of this project will be to generate Java clients for APIs rather than server-side (except for one primary target that will be used for unit testing). The main reason for this is really the huge number of server-side frameworks that are out there. Yet to be decided!

**Features**
* Very clean minimal generated code (reused logic is in runtime libraries (server or client))
* Immutable generated schema classes (none of this mutable Java beans getters-and-setters rubbish)
* Extensively unit tested (and easy to add more to either demonstrate problems or correctness)
* Supports Java 8, 11, 17 (CI)
* Supports Spring Boot 2.x, 3.x server-side
* Supports polymorphism `oneOf`, `anyOf`, `allOf` with or without discriminators
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
* use Java HttpsURLConnection for HTTP interactions or use Apache Httpclient 5.x (raise an issue to add another Http library)

**Status**: released to Maven Central

## Limitations
* `allOf` only with object schemas
* parameter types like explode, label, deepObject not implemented yet
* multipart and form url encoded request bodied implemented on client, not server yet.
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
            </configuration>
        </execution>
    </executions>
</plugin>
```

## General advice
* As much as possible make sure you put your types in the `#/components/schemas` section of your openapi yaml/json file (use `$ref`!). The same goes for responses, pathItems, and anything else that can be referred to with a `$ref`. Don't use anonymous types, it makes for an ugly experience with generated code.
* Specify `format: int32` on integers to ensure you end up with `int/integer` types in generated code
* Be sure to specify the properties that are mandatory (using `required:`)
* Set an `operationId` field for every path entry to ensure you get sensible generated method names (in client and server)

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
[PetSearch.java](src/docs/PetSearch.java), [PetByAge.java](src/docs/PetByAge.java), [PetByType.java](src/docs/PetByType.java)

[AnyOfSerializer.java](openapi-codegen-runtime/src/main/java/org/davidmoten/oa3/codegen/runtime/AnyOfSerializer.java), [PolymorphicDeserializer.java](openapi-codegen-runtime/src/main/java/org/davidmoten/oa3/codegen/runtime/PolymorphicDeserializer.java)

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
    .latitude(Latitude.value(-10))
    .longitude(Longitude.value(140))
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
    .lat(Latitude.value(-35f))
    .lon(Longitude.value(142f))
    .radiusNm(20)
    .build());
```
Note that if the first field is mandatory you can omit the `builder()` method call:
```java
Geometry g = Geometry.of(Circle
    .lat(Latitude.value(-35f))
    .lon(Longitude.value(142f))
    .radiusNm(20)
    .build());
```

## Validation
Enabled/disabled by setting a new `Globals.config`. Configurable on a class-by-class basis.

## Nulls
The classes generated by *openapi-codegen* **do not allow null parameters** in public methods.

OpenAPI v3 allows the specification of fields with `nullable` set to `true`. When `nullable` is true for a property (like `thing`)
then the following fragments must be distinguishable in serialization and deserialization:

```json
{ "thing" : null }
```
and 
```json
{}
```
This is achieved using the special class `JsonNullable` from the Jackson library. When you want an entry like `"thing" : null` to be
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

Docusign api needs work here because has more than 255 fields in an object which exceeds Java contructor limits.

To run tests on the above apis call this:
```bash
./test-all.sh 
```
This script ensures that the code generated from the above large test apis compiles and does so in many separate generation and compile steps because the apis generate so much code that the compilation step runs out of memory on my devices!

## TODO
* ~~`additionalProperties` (Dictionary) support~~, done
* generate javadoc for fields
* `not` support
* delegate constructors using `this(`
* workaround JsonCreator not being able to pass `5` into a double argument, must be `5.0` (https://github.com/FasterXML/jackson-core/issues/532)
* document limited support for parameter style with spring-boot rest
* support objects with more than 255 fields (max parameter number in Java gets exceeded in object constructor)
* support form-style request bodies on server-side (multipart, urlencoded). Client side support done (not comprehensive).
* support more parameter styles
* support xml
* write docs
* support 3.1 features (type arrays, null instead of nullable, contentMediaType and contentEncoding for file payloads)
