package org.davidmoten.oa3.generator.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
import org.davidmoten.oa3.generator.Definition;
import org.davidmoten.oa3.generator.Generator;
import org.davidmoten.oa3.generator.Packages;

@Mojo(name = "generate",defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public final class GenerateMojo extends AbstractMojo {

    @Parameter(name = "sources")
    private FileSet sources;

    @Parameter(name = "outputDirectory", defaultValue = "${project.build.directory}/generated-sources/java")
    private File outputDirectory;

    @Parameter(name = "basePackage", defaultValue = "generated")
    private String basePackage;

    @Parameter(name = "charset", defaultValue = "UTF-8")
    private String charset;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

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
                String definition = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                Packages packages = new Packages(basePackage);
                Definition d = new Definition(definition, packages, outputDirectory, x -> x);
                new Generator(d).generate();
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
