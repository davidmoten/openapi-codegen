package org.davidmoten.oa3.codegen.generator.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.davidmoten.text.utils.WordWrap;

import com.github.davidmoten.guavamini.Maps;
import com.github.davidmoten.guavamini.Preconditions;

public final class Javadoc {

    private static final int MAX_JAVADOC_WIDTH = 80;

    public static boolean printJavadoc(CodePrintWriter out, Indent indent, String text, boolean isHtml) {
        Preconditions.checkNotNull(text);
        return printJavadoc(out, Optional.of(text), Collections.emptyList(), Optional.empty(), Optional.empty(),
                Collections.emptyMap(), isHtml, Maps.empty());
    }

    public static boolean printJavadoc(CodePrintWriter p, Optional<String> text, List<Annotation> annotations,
            Optional<String> preamble, Optional<String> returns, Map<String, String> parameterDoc, boolean isHtml,
            Map<String, String> throwing) {
        boolean hasText = text.isPresent() || !annotations.isEmpty() || returns.isPresent();
        boolean addParagraph = false;
        if (hasText) {
            p.println();
            p.line("/**");
            if (preamble.isPresent()) {
                p.line(" * %s", encodeAndWrapForJavadoc(preamble.get(), p.indent(), false));
                addParagraph = true;
            }
        }
        if (text.isPresent()) {
            if (addParagraph) {
                p.line(" * <p>");
            }
            p.line(" * %s", encodeAndWrapForJavadoc(text.get(), p.indent(), isHtml));
        }
        annotations.forEach(a -> {
            p.line(" * <p>");
            p.line(" * <b>%s</b>", a.getTerm());
            if (a.getString().isPresent()) {
                p.line(" * <p>");
                p.line(" * %s", a.getString().get());
            }
            if (a.getBool().isPresent()) {
                p.line(" * <p>");
                p.line(" * %s", a.getBool().get());
            }
            for (String record : a.getRecords()) {
                p.line(" * <p>");
                p.line(" * %s", record);
            }
        });
        if (hasText) {
            boolean first = true;
            for (Entry<String, String> entry : parameterDoc.entrySet()) {
                if (first) {
                    p.line(" * ");
                    first = false;
                }
                p.line(" * @param %s", entry.getKey());
                String encoded = encodeAndWrapForJavadoc(entry.getValue(), true,
                        String.format("\n%s *            ", p.indent()));
                p.line(" *            %s", encoded);
            }
            if (returns.isPresent()) {
                if (first) {
                    p.line(" * ");
                }
                p.line(" * @return %s", returns.get());
            }
            if (!throwing.isEmpty()) {
                if (first) {
                    p.line(" * ");
                }
                throwing //
                        .entrySet() //
                        .stream() //
                        .forEach(entry -> {
                            p.line(" * @throws %s", entry.getKey());
                            p.line(" *             %s", entry.getValue());
                        });
            }
            p.line(" */");
        }
        return hasText;
    }

    private static String simplifyParameterHtml(String html) {
        if (!html.startsWith("<p>")) {
            return html;
        } else if (html.substring(3).contains("<")) {
            // has html tags other than <p>
            return html;
        } else {
            return html.substring(3);
        }
    }

    private static String encodeAndWrapForJavadoc(String s, Indent indent, boolean isHtml) {
        return encodeAndWrapForJavadoc(s, isHtml, String.format("\n%s * ", indent));
    }

    private static String encodeAndWrapForJavadoc(String s, boolean isHtml, String linePrefix) {
        s = s.trim().replace("{@", "zxxz");
        if (!isHtml) {
            s = wrap(s);
        }
        return encodeJavadoc(s, isHtml) //
                .replace("\n", linePrefix) //
                .replace("zxxz", "{@");
    }

    private static String encodeJavadoc(String x, boolean isHtml) {
        x = x.replace("@", "&#064;") //
                .replace("\\", "{@literal \\}") //
        ;
//                .replace("&", "&amp;");
        if (isHtml) {
            x = x.replace("</p>", "").replace("*/", "*&#47;");
        } else {
            x = x.replace("<", "&lt;") //
                    .replace(">", "&gt;");
        }
        return x;
    }

    private static String wrap(String s) {
        return WordWrap //
                .from(s) //
                .breakWords(false) //
                .extraWordChars("0123456789") //
                .maxWidth(MAX_JAVADOC_WIDTH) //
                .newLine("\n") //
                .wrap() //
                .trim();
    }

}
