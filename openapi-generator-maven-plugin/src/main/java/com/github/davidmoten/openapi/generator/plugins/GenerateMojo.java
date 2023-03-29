package com.github.davidmoten.openapi.generator.plugins;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.io.FileUtils;

@Mojo(name = "generate")
public final class GenerateMojo extends AbstractMojo {

    @Parameter(name = "sources")
    private FileSet sources;

    @Parameter(name = "outputDirectory", defaultValue = "${project.build.directory}/generated-diagrams/")
    private File outputDirectory;

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
            for (File file: files) {
                getLog().info(file.toString());
            }
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private static String commaSeparate(List<String> list) {
        return list.stream().collect(Collectors.joining(","));
    }

}
