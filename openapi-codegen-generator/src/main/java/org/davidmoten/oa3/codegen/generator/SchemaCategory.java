package org.davidmoten.oa3.codegen.generator;

public enum SchemaCategory {
    
    SCHEMA ("schema"), PATH("path"), PATH_ITEM("pathitem"), RESPONSE("response"), REQUEST_BODY("requestbody"), PARAMETER("parameter");

    private final String packageFragment;

    SchemaCategory(String packageFragment){
        this.packageFragment = packageFragment;
    }

    public String getPackageFragment() {
        return packageFragment;
    }
    
}
