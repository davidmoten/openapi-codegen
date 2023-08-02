#!/bin/bash
mkdir -p ../src/docs
cp target/generated-sources/java/org/davidmoten/oa3/codegen/test/main/schema/Car.java ../src/docs/Car.java 
cp target/generated-sources/java/org/davidmoten/oa3/codegen/test/main/schema/Bike.java ../src/docs/Bike.java
cp target/generated-sources/java/org/davidmoten/oa3/codegen/test/main/schema/Vehicle.java ../src/docs/Vehicle.java
cp target/generated-sources/java/org/davidmoten/oa3/codegen/test/main/schema/Geometry.java ../src/docs/Geometry.java
cp target/generated-sources/java/org/davidmoten/oa3/codegen/test/main/schema/Circle.java ../src/docs/Circle.java
cp target/generated-sources/java/org/davidmoten/oa3/codegen/test/main/schema/Rectangle.java ../src/docs/Rectangle.java
cp target/generated-sources/java/org/davidmoten/oa3/codegen/test/library/client/Client.java ../src/docs/Client.java
