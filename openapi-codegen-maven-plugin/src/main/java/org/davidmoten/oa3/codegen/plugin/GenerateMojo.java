package org.davidmoten.oa3.codegen.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.io.FileUtils;
import org.davidmoten.oa3.codegen.generator.Definition;
import org.davidmoten.oa3.codegen.generator.Generator;
import org.davidmoten.oa3.codegen.generator.Packages;
import org.davidmoten.oa3.codegen.generator.SpringBootGenerator;
import org.davidmoten.oa3.codegen.generator.internal.Util;

import com.github.davidmoten.guavamini.Sets;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = false)
public final class GenerateMojo extends AbstractMojo {

    @Parameter(name = "sources")
    private FileSet sources;

    @Parameter(name = "outputDirectory", defaultValue = "${project.build.directory}/generated-sources/java")
    private File outputDirectory;

    @Parameter(name = "basePackage", defaultValue = "openapi.generated")
    private String basePackage;

    @Parameter(name = "charset", defaultValue = "UTF-8")
    private String charset;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(name = "includeSchemas")
    private List<String> includeSchemas;

    @Parameter(name = "excludeSchemas")
    private List<String> excludeSchemas;

    @Parameter(name = "mapIntegerToBigInteger", defaultValue = "false")
    private boolean mapIntegerToBigInteger;

    @Parameter(name = "failOnParseErrors", defaultValue = "true")
    private boolean failOnParseErrors;
    
    @Parameter(name = "generator")
    private String generator; 

    @Override
    public void execute() throws MojoExecutionException {
        if (sources == null) {
            sources = new FileSet();
            sources.setDirectory(new File(//
                    project.getBasedir(), //
                    "src" + File.separator //
                            + "main" + File.separator //
                            + "openapi").getAbsolutePath());
        }
        getLog().info("sources=" + sources);
        try {
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
                        Sets.newHashSet(Util.orElse(includeSchemas, Collections.emptyList())),
                        Sets.newHashSet(Util.orElse(excludeSchemas, Collections.emptyList())), mapIntegerToBigInteger,
                        failOnParseErrors);
                new Generator(d).generate();
                if("spring".equals(generator)) {
                    new SpringBootGenerator(d).generate();
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
