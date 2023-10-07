package org.davidmoten.oa3.codegen.generator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.ScalarStyle;

public class YamlTest {

    @Test
    public void test() throws FileNotFoundException, IOException {
        Load load = new Load(LoadSettings.builder().build());
        try (InputStream in = new FileInputStream("../openapi-codegen-maven-plugin-test/src/main/openapi/main.yml")) {
            Iterator<Object> it = load.loadAllFromInputStream(in).iterator();
            DumpSettings settings = DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED).build();
            Dump dump = new Dump(settings);
            System.out.println(dump.dumpAllToString(it));
        }
    }

}
