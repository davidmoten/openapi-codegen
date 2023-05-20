package org.davidmoten.oa3.codegen.plugin;

import static org.davidmoten.oa3.codegen.util.Util.orElse;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.io.FileUtils;
import org.davidmoten.oa3.codegen.generator.ClientServerGenerator;
import org.davidmoten.oa3.codegen.generator.Definition;
import org.davidmoten.oa3.codegen.generator.DownloadExtras;
import org.davidmoten.oa3.codegen.generator.Generator;
import org.davidmoten.oa3.codegen.generator.Packages;

import com.github.davidmoten.guavamini.Sets;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = false)
public final class GenerateMojo extends AbstractMojo {

    @Parameter(name = "sources")
    private FileSet sources;

    @Parameter(name = "outputDirectory", defaultValue = "${project.build.directory}/generated-sources/java")
    private File outputDirectory;

    @Parameter(name = "basePackage", defaultValue = "openapi.generated")
    private String basePackage;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(name = "includeSchemas")
    private List<String> includeSchemas;

    @Parameter(name = "excludeSchemas")
    private List<String> excludeSchemas;

    @Parameter(name = "mapIntegerToBigInteger", defaultValue = "false")
    private boolean mapIntegerToBigInteger;

    @Parameter(name = "mapNumberToBigDecimal", defaultValue = "false")
    private boolean mapNumberToBigDecimal;

    @Parameter(name = "failOnParseErrors", defaultValue = "true")
    private boolean failOnParseErrors;

    @Parameter(name = "generator", defaultValue = "spring2")
    private String generator;

    @Parameter(name = "generateService", defaultValue = "true")
    private boolean generateService;

    @Parameter(name = "generateClient", defaultValue = "true")
    private boolean generateClient;

    @Parameter(name = "downloadList")
    private File downloadList;

    @Parameter(name = "cacheDirectory", defaultValue = "${project.basedir}/.openapi-codegen/cache")
    private File cacheDirectory;

    @Parameter(name = "skip", defaultValue = "false")
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            System.out.println("[INFO] skipping");
            return;
        }
        if (downloadList != null) {
            DownloadExtras.run(downloadList, cacheDirectory);
        }
        File defaultSourceDirectory = new File(//
                project.getBasedir(), //
                "src" + File.separator //
                        + "main" + File.separator //
                        + "openapi");
        if (sources == null) {
            sources = new FileSet();
            sources.setDirectory(defaultSourceDirectory.getAbsolutePath());
        }
        getLog().info("sources=" + sources);
        try {
            if (sources.getDirectory() == null) {
                sources.setDirectory(defaultSourceDirectory.getAbsolutePath());
            }
            if (sources.getIncludes().isEmpty()) {
                sources.addInclude("**/*.yml");
                sources.addInclude("**/*.yaml");
            }
            List<File> files = FileUtils.getFiles(new File(sources.getDirectory()),
                    commaSeparate(sources.getIncludes()), commaSeparate(sources.getExcludes()));
            for (File file : files) {
                getLog().info(file.toString());
                String definition = file.toURI().toURL().toExternalForm();
                Packages packages = new Packages(basePackage);
                Definition d = new Definition(definition, packages, outputDirectory, x -> x,
                        Sets.newHashSet(orElse(includeSchemas, Collections.emptyList())),
                        Sets.newHashSet(orElse(excludeSchemas, Collections.emptyList())), mapIntegerToBigInteger,
                        mapNumberToBigDecimal, failOnParseErrors, Optional.ofNullable(generator));
                new Generator(d).generate();
                if (generateService || generateClient) {
                    ClientServerGenerator g = new ClientServerGenerator(d);
                    if (generateService) {
                        g.generateServer();
                    }
                    if (generateClient) {
                        g.generateClient();
                    }
                }
            }
            project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private static String commaSeparate(List<String> list) {
        return list.stream().collect(Collectors.joining(","));
    }

}
