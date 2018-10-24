package me.changchao.spring.databindertest;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertyNameAliases;
import org.springframework.boot.context.properties.source.ConfigurationPropertyState;
import org.springframework.boot.context.properties.source.IterableConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

@SpringBootApplication
public class DataBinderTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataBinderTestApplication.class, args);
	}

	@Bean
	Foo foo(ConfigurableApplicationContext applicationContext, ConfigurableEnvironment env,
			Optional<ConversionService> conversionService) {
		MapConfigurationPropertySource mapSource = new MapConfigurationPropertySource();

		for (Iterator<PropertySource<?>> it = env.getPropertySources().iterator(); it.hasNext();) {
			PropertySource<?> propertySource = it.next();
			if (propertySource instanceof MapPropertySource) {
				mapSource.putAll(((MapPropertySource) propertySource).getSource());
			}
		}
		ConfigurationPropertyName fooPropertyName = ConfigurationPropertyName.of("foo");

		IterableConfigurationPropertySource fooPropertySource = mapSource
				.filter(a -> a != null && (fooPropertyName.isAncestorOf(a) || fooPropertyName.equals(a)));
		ConfigurationPropertyNameAliases aliases = new ConfigurationPropertyNameAliases();

		aliases.addAliases("foo.bar", "foo.other-name");

		aliases.addAliases("foo.bar[0]", "foo.other-name[0]");
		aliases.addAliases("foo.bar[1]", "foo.other-name[1]");

//		aliases.addAliases("foo.other-name[0]", "foo.bar[0]");
//		aliases.addAliases("foo.other-name[1]", "foo.bar[1]");


		aliases.addAliases("foo.other-name[0].name", "foo.bar[0].name");
		aliases.addAliases("foo.other-name[1].name", "foo.bar[1].name");

//		aliases.addAliases("foo.bar[0].name", "foo.other-name[0].name");
//		aliases.addAliases("foo.bar[1].name", "foo.other-name[2].name");

		IterableConfigurationPropertySource withAliases = fooPropertySource.withAliases(aliases);
		ConfigurationPropertyState contains = withAliases.containsDescendantOf(ConfigurationPropertyName.of("foo.bar"));
		ConfigurationProperty configurationProperty = withAliases
				.getConfigurationProperty(ConfigurationPropertyName.of("foo.bar"));

		Consumer<PropertyEditorRegistry> propertyEditorInitializer = applicationContext
				.getBeanFactory()::copyRegisteredEditorsTo;

		Binder binder = new Binder(Collections.singleton(withAliases), new PropertySourcesPlaceholdersResolver(env),
				conversionService.orElse(null), propertyEditorInitializer);

		BindResult<Foo> bind = binder.bind("foo", Foo.class);

		Iterator<ConfigurationPropertyName> names = withAliases.iterator();
		names.forEachRemaining(a -> System.out.println(a));

		Foo foo = bind.get();
		System.out.println("foo=" + foo);
		return foo;
		// return null;
	}

}
