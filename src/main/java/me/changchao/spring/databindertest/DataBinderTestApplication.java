package me.changchao.spring.databindertest;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.source.ConfigurationPropertyNameAliases;
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
			ConversionService conversionService) {
		MapConfigurationPropertySource mapSource = new MapConfigurationPropertySource();

		for (Iterator<PropertySource<?>> it = env.getPropertySources().iterator(); it.hasNext();) {
			PropertySource<?> propertySource = it.next();
			if (propertySource instanceof MapPropertySource) {
				mapSource.putAll(((MapPropertySource) propertySource).getSource());
			}
		}

		ConfigurationPropertyNameAliases aliases = new ConfigurationPropertyNameAliases();
		aliases.addAliases("foo.other-name[0].name", "foo.bar[0].name");
		aliases.addAliases("foo.other-name[1].name", "foo.bar[1].name");
		aliases.addAliases("foo.other-name", "foo.bar");
		// aliases.addAliases("foo.other-name[n]", "foo.bar[n]");
		IterableConfigurationPropertySource withAliases = mapSource.withAliases(aliases);

		Consumer<PropertyEditorRegistry> propertyEditorInitializer = applicationContext
				.getBeanFactory()::copyRegisteredEditorsTo;
		Binder binder = new Binder(Collections.singleton(withAliases), new PropertySourcesPlaceholdersResolver(env),
				conversionService, propertyEditorInitializer);

		BindResult<Foo> bind = binder.bind("foo", Foo.class);
		Map<String, Object> underlyingSource = (Map<String, Object>) mapSource.getUnderlyingSource();
		System.out.println(underlyingSource.keySet());
		return bind.get();
	}

}
