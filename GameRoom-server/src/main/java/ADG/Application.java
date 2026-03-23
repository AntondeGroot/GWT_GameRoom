package ADG;

import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
@ServletComponentScan
@EnableScheduling
public class Application
    extends SpringBootServletInitializer {

  public static void main(String[] args) {
    SpringApplication.run(Application.class,
                          args);
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(Application.class);
  }

  @Component
  public static class EmbeddedServletContainerConfig
      implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
      // getResource("/").getFile() is broken inside a fat JAR/WAR because the URL
      // uses the jar: protocol, not file:. Use ClassPathResource instead, which
      // handles both exploded (dev) and packaged (Pi) deployments.
      // Note: the project uses IsSerializable throughout, so GWT-RPC works even
      // when the document root cannot be set (no serialisation policy file needed).
      try {
        File launcherDir = new ClassPathResource("launcherDir").getFile();
        if (launcherDir.exists()) {
          factory.setDocumentRoot(launcherDir);
        }
      } catch (IOException e) {
        // Running from a JAR — launcherDir is not extractable as a File.
        // GWT-RPC still works because all shared classes implement IsSerializable.
      }
    }
  }

}
