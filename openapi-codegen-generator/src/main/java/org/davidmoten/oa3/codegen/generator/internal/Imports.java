package org.davidmoten.oa3.codegen.generator.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class Imports {

    private final String fullClassName;
    private final Predicate<String> simpleNameInPackage;
    private final String basePackagePrefix;
    // simpleName (may be full class name if already present) to fullClassName
    private final Map<String, String> map = new HashMap<>();

    public Imports(String fullClassName, Predicate<String> simpleNameInPackage) {
        this.fullClassName = fullClassName;
        this.simpleNameInPackage = simpleNameInPackage;
        this.basePackagePrefix = packagePrefix(fullClassName);
        add(fullClassName);
    }

    public String add(Class<?> cls) {
        return add(cls.getCanonicalName().replace("$", "."));
    }

    public String add(String className) {
        if (className.endsWith("[]") && Util.isPrimitiveFullClassName(className.substring(0, className.length() - 2))) {
            // don't add byte[] etc to imports
            return className;
        }
        final String simpleName = simpleName(fullClassName, className);
        String c = map.get(simpleName);

        String pp = packagePrefix(className);
        if (className.equals(fullClassName)) {
            return simpleName;
        } else if (c == null && !simpleName.equals(simpleName(fullClassName)) && (!basePackagePrefix.equals(pp) //
                || !simpleNameInPackage.test(packagePrefix(fullClassName) + firstSegment(simpleName)))) {
            map.put(simpleName, className);
            return simpleName;
        } else if (c != null && c.equals(className)) {
            return simpleName;
        } else {
            return className;
        }
    }

    private static String packagePrefix(String fullClassName) {
        int i = fullClassName.lastIndexOf('.');
        if (i == -1) {
            return "";
        } else {
            return fullClassName.substring(0, i + 1);
        }
    }

    private static String simpleName(String baseClassName, String className) {
        if(className.equals(baseClassName)) {
            return simpleName(className);
        } else if (className.startsWith(baseClassName + ".")){
            return className.substring(baseClassName.length() + 1);
        } else {
            return simpleName(className);
        }
    }

    private static String simpleName(String className) {
        final String simpleName;
        int i = className.lastIndexOf('.');
        if (i == -1) {
            simpleName = className;
        } else {
            simpleName = className.substring(i + 1);
        }
        return simpleName;
    }

    @Override
    public String toString() {
        String pkgFullClassName = pkg(fullClassName);
        String x = map //
                .values() //
                .stream() //
                .sorted() //
//                .filter(c -> !c.startsWith("java.lang.")) //
                .filter(c -> !c.equals("boolean")) //
                .filter(c -> !c.equals("short")) //
                .filter(c -> !c.equals("float")) //
                .filter(c -> !c.equals("double")) //
                .filter(c -> !c.equals("int")) //
                .filter(c -> !c.equals("byte")) //
                .filter(c -> !c.equals("long")) //
                .filter(c -> !c.equals(fullClassName)) // ensure that if in same pkg as fullClassName that we don't need
                                                       // to specify an import
                .filter(c -> !c.startsWith(fullClassName + ".")) // is member class
                .filter(c -> !pkg(c).equals(pkgFullClassName)) //
                .map(process()) //
                .collect(Collectors.joining("\n"));
        if (!x.isEmpty()) {
            x = x + "\n";
        }
        return x;
    }

    private static Function<String, String> process() {
        return new Function<String, String>() {

            String previous;

            @Override
            public String apply(String c) {
                String firstSegment = firstSegment(c);
                boolean insertBlankLine = previous != null && !firstSegment.equals(previous);
                previous = firstSegment;
                return (insertBlankLine ? "\n" : "") + "import " + c + ";";
            }
        };
    }

    private static String pkg(String fullClassName) {
        int i = fullClassName.lastIndexOf('.');
        if (i == -1) {
            return "";
        } else {
            return fullClassName.substring(0, i);
        }
    }

    private static String firstSegment(String s) {
        int i = s.indexOf('.');
        if (i == -1) {
            return s;
        } else {
            return s.substring(0, i);
        }
    }

    public String fullClassName() {
        return fullClassName;
    }

}