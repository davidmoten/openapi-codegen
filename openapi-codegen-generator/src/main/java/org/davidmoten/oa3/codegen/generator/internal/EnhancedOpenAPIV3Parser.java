package org.davidmoten.oa3.codegen.generator.internal;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.yaml.snakeyaml.LoaderOptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.swagger.v3.core.util.Yaml31;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.parser.exception.ReadContentException;
import io.swagger.v3.parser.util.ClasspathHelper;
import io.swagger.v3.parser.util.RemoteUrl;

public final class EnhancedOpenAPIV3Parser extends OpenAPIV3Parser {
    
    private boolean initialized;

    /**
     * Encoding of the resource content with OpenAPI spec to parse.
     */
    private static String encoding = StandardCharsets.UTF_8.displayName();
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAPIV3Parser.class);

    public EnhancedOpenAPIV3Parser() {

    }

    private synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            YAMLFactory yamlFactory = createUnlimitedSizeYamlFactory();
            ObjectMapper mapper = new ObjectMapper(yamlFactory);
            {
                Field fld = OpenAPIV3Parser.class.getDeclaredField("YAML_MAPPER");
                fld.setAccessible(true);
                fld.set(this, mapper);
                LOGGER.debug("OpenAPIV3Parser.YAML_MAPPER overriden via reflection to support unlimited code points");
            }
            {
                Yaml31.mapper();
                Field fld = Yaml31.class.getDeclaredField("mapper");
                fld.setAccessible(true);
                fld.set(this, mapper);
                LOGGER.debug("Yaml31.mapper overriden via reflection to support unlimited code points");
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            LOGGER.warn("could not modify OpenAPIV3Parser.YAML_MAPPER", e);
            throw new RuntimeException(e);
        }
    }

    private static YAMLFactory createUnlimitedSizeYamlFactory() {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setCodePointLimit(Integer.MAX_VALUE);
        YAMLFactory yamlFactory = YAMLFactory.builder().loaderOptions(loaderOptions).build();
        return yamlFactory;
    }

    @Override
    public SwaggerParseResult readLocation(String url, List<AuthorizationValue> auth, ParseOptions options) {
        init();
        try {
            final String content = doubleQuoteStrings(readContentFromLocation(url, emptyListIfNull(auth)));
            LOGGER.debug("Loaded raw data: {}", content);
            return readContents(content, auth, options, url);
        } catch (ReadContentException e) {
            LOGGER.warn("Exception while reading:", e);
            return SwaggerParseResult.ofError(e.getMessage());
        }
    }

    /**
     * Uses snakeyaml-engine to parse yaml as YAML 1.2 and stop replacing on, off,
     * yes, no with true or false boolean values.
     * 
     * @param data json or yaml OpenAPI definition
     * @return json data untouched or if yaml returns data converted to have double
     *         quoted strings and !! types so YAML 1.1 parser used by swagger-parser
     *         will not do annoying replacements of on, off, yes, no with boolean
     *         true and false values.
     */
    private static String doubleQuoteStrings(String data) {
        if (data.startsWith("{")) {
            return data;
        } else {
            Load load = new Load(LoadSettings.builder().setCodePointLimit(Integer.MAX_VALUE).build());
            Iterator<Object> it = load.loadAllFromString(data).iterator();
            DumpSettings settings = DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED).build();
            Dump dump = new Dump(settings);
            return dump.dumpAllToString(it);
        }
    }

    // copied from OpenAPIV3Parser, added Locale.ENGLISH to toLowerCase calls to
    // satisfy spotbugs
    // if this method was protected in OpenAPIV3Parser I wouldn't need to copy it
    // (that
    // would be nice!)
    private String readContentFromLocation(String location, List<AuthorizationValue> auth) {
        final String adjustedLocation = location.replaceAll("\\\\", "/");
        try {
            if (adjustedLocation.toLowerCase(Locale.ENGLISH).startsWith("http")) {
                return RemoteUrl.urlToString(adjustedLocation, auth);
            } else if (adjustedLocation.toLowerCase(Locale.ENGLISH).startsWith("jar:")) {
                final InputStream in = new URI(adjustedLocation).toURL().openStream();
                return IOUtils.toString(in, encoding);
            } else {
                final String fileScheme = "file:";
                final Path path = adjustedLocation.toLowerCase(Locale.ENGLISH).startsWith(fileScheme)
                        ? Paths.get(URI.create(adjustedLocation))
                        : Paths.get(adjustedLocation);
                if (Files.exists(path)) {
                    return FileUtils.readFileToString(path.toFile(), encoding);
                } else {
                    return ClasspathHelper.loadFileFromClasspath(adjustedLocation);
                }
            }
        } catch (SSLHandshakeException e) {
            final String message = String.format(
                    "Unable to read location `%s` due to a SSL configuration error. It is possible that the server SSL certificate is invalid, self-signed, or has an untrusted Certificate Authority.",
                    adjustedLocation);
            throw new ReadContentException(message, e);
        } catch (Exception e) {
            throw new ReadContentException(String.format("Unable to read location `%s`", adjustedLocation), e);
        }
    }

    private static <T> List<T> emptyListIfNull(List<T> list) {
        return Objects.isNull(list) ? new ArrayList<>() : list;
    }

}
