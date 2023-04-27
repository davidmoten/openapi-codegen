package org.davidmoten.oa3.codegen.generator;

enum SchemaCategory {

    SCHEMA("schema", "#/components/schemas/"), //
    PATH("path", "#/components/paths/"), //
    PATH_ITEM("pathitem", "#/components/pathItems/"), //
    RESPONSE("response", "#/components/responses/"), //
    REQUEST_BODY("requestbody", "#/components/requestBodies/"), //
    PARAMETER("parameter", "#/components/parameters/");

    private final String packageFragment;
    private final String refPrefix;

    SchemaCategory(String packageFragment, String refPrefix) {
        this.packageFragment = packageFragment;
        this.refPrefix = refPrefix;
    }

    String getPackageFragment() {
        return packageFragment;
    }

    String refPrefix() {
        return refPrefix;
    }

}
