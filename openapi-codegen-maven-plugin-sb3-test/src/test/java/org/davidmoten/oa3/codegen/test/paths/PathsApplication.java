package org.davidmoten.oa3.codegen.test.paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

@EnableConfigurationProperties
@SpringBootApplication
public class PathsApplication {
    
    @Bean
    public ServletRegistrationBean<MultipartServlet> servletRegistrationBean(){
        return new ServletRegistrationBean<>(new MultipartServlet(),"/upload");
    }
    
    @Bean
    public ServletRegistrationBean<FormServlet> servletRegistrationBean2(){
        return new ServletRegistrationBean<>(new FormServlet(),"/submit");
    }
    
    @Bean(name = "multipartResolver")
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    public static void main(String[] args) {
        SpringApplication.run(PathsApplication.class, args);
    }

}