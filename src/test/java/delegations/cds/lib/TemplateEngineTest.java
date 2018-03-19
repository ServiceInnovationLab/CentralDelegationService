package delegations.cds.lib;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.wildfly.swarm.fractions.PropertiesUtil;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;

@RunWith(JUnit4.class)
public class TemplateEngineTest {

    Properties messages;
    TemplateEngine engine;
    StandardMessageResolver resolver;


    @Before
    public void setup() throws Exception {
        messages = PropertiesUtil.loadProperties("i18n/messages.properties");

        resolver = new StandardMessageResolver();
        resolver.setDefaultMessages(messages);
        engine = new TemplateEngine();
        engine.setMessageResolver(resolver);
    }

    @Test
    public void renderBasicOne() {
        String template = "<p th:text=${greeting}>Hello World</p>";
        Context context = new Context();
        context.setVariable("greeting", "Hello Bob");

        StringWriter writer = new StringWriter();
        engine.process(template, context, writer);
        String result = writer.toString();
        assertThat(result).isEqualTo("<p>Hello Bob</p>");
    }

    @Test
    public void renderBasicTwo() {
        String template = "<p th:text=${greeting}></p>";

        Context context = new Context();
        context.setVariable("greeting", "Hello Bob");

        String result = engine.process(template, context);

        assertThat(result).isEqualTo("<p>Hello Bob</p>");

    }

    @Test
    public void useProps() {
        assertThat(messages.getProperty("greeting")).isEqualTo("Hello");
        assertThat(messages.getProperty("greeting.casual")).isEqualTo("Hi");
    }

    @Test
    public void renderBasicThree() {
        String template = "<p th:text='|Hello ${name}|'></p>";

        Context context = new Context();
        context.setVariable("name", "Bob");

        String result = engine.process(template, context);

        assertThat(result).isEqualTo("<p>Hello Bob</p>");

    }

    @Test
    public void renderHtmlOne() throws Exception {
        String path = "src/test/resources/templates/one.html";
        List<String> lines = Files.readAllLines(Paths.get(path), Charsets.UTF_8);
        String template = Joiner.on("\n").join(lines);

        Context context = new Context();
        context.setVariable("greeting", "Hello Bob");

        String result = engine.process(template, context);

        assertThat(result).contains("<p>Hello Bob</p>");

    }

    @Test
    public void renderHtmlTwo() throws Exception {
        String path = "src/test/resources/templates/two.html";
        List<String> lines = Files.readAllLines(Paths.get(path), Charsets.UTF_8);
        String template = Joiner.on("\n").join(lines);

        Context context = new Context();
        context.setVariable("name", "Bob");


        String result = engine.process(template, context);

        assertThat(result).contains("<p>Hello Bob</p>");

    }






}
