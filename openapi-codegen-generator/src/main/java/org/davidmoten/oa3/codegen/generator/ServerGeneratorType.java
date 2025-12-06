package org.davidmoten.oa3.codegen.generator;

import java.util.Locale;

public enum ServerGeneratorType {

    SPRING_BOOT_2, SPRING_BOOT_3, SPRING_BOOT_4;

    public static ServerGeneratorType from(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Server generator type cannot be null");
        }
        switch (type.toLowerCase(Locale.ENGLISH)) {
        case "spring2":
            return SPRING_BOOT_2;
        case "springboot2":
            return SPRING_BOOT_2;
        case "spring3":
            return SPRING_BOOT_3;
        case "springboot3":
            return SPRING_BOOT_3;
        case "spring4":
            return SPRING_BOOT_4;
        case "springboot4":
            return SPRING_BOOT_4;
        default:
            throw new IllegalArgumentException("Unknown server generator type: " + type);
        }
    }

}
