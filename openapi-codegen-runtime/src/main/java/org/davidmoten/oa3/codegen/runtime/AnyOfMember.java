package org.davidmoten.oa3.codegen.runtime;

public class AnyOfMember {
    
    private Class<?> cls;
    private boolean nullable;

    private AnyOfMember(Class<?> cls, boolean nullable) {
        this.cls = cls;
        this.nullable = nullable;
    }
    
    public static AnyOfMember nonNullable(Class<?> cls) {
        return new AnyOfMember(cls, false);
    }
    
    public static AnyOfMember nullable(Class<?> cls) {
        return new AnyOfMember(cls, true);
    }
    
    public Class<?> cls() {
        return cls;
    }
    
    public boolean nullable() {
        return nullable;
    }

}
