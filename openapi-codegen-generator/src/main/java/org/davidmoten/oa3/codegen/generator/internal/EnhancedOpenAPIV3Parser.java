package org.davidmoten.oa3.codegen.generator.internal;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.parser.exception.ReadContentException;
import io.swagger.v3.parser.util.ClasspathHelper;
import io.swagger.v3.parser.util.RemoteUrl;

public final class EnhancedOpenAPIV3Parser extends OpenAPIV3Parser {

    /**
     * Encoding of the resource content with OpenAPI spec to parse.
     */
    private static String encoding = StandardCharsets.UTF_8.displayName();
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAPIV3Parser.class);

    @Override
    public SwaggerParseResult readLocation(String url, List<AuthorizationValue> auth, ParseOptions options) {
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
     * @return data converted to have double quoted strings so YAML 1.1 parser will
     *         work ok on it.
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

    private String readContentFromLocation(String location, List<AuthorizationValue> auth) {
        final String adjustedLocation = location.replaceAll("\\\\", "/");
        try {
            if (adjustedLocation.toLowerCase().startsWith("http")) {
                return RemoteUrl.urlToString(adjustedLocation, auth);
            } else if (adjustedLocation.toLowerCase().startsWith("jar:")) {
                final InputStream in = new URI(adjustedLocation).toURL().openStream();
                return IOUtils.toString(in, encoding);
            } else {
                final String fileScheme = "file:";
                final Path path = adjustedLocation.toLowerCase().startsWith(fileScheme)
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
