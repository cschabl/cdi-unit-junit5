package de.schablinski.cdiunit;

import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jglue.cdiunit.internal.TestConfiguration;
import org.jglue.cdiunit.internal.Weld11TestUrlDeployment;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * CdiUnitExtension is a JUnit5 test instance factory that creates test instances as CDI beans.
 */
public class CdiUnitExtension implements TestInstanceFactory, AfterEachCallback
{
    private static final String ABSENT_CODE_PREFIX = "Absent Code attribute in method that is not native or abstract in class file ";

    private Weld weld;

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext)
        throws TestInstantiationException
    {
        Class<?> testClass = extensionContext.getTestClass()
            .orElseThrow(() -> new TestInstantiationException("test class required"));

        WeldContainer container;

        try {
            // extensionContext.getTestMethod() is an empty optional, here
            final TestConfiguration testConfig = createTestConfiguration(testClass, null);

            weld = new Weld() {

                // override for Weld 2.0, 3.0
                protected Deployment createDeployment(ResourceLoader resourceLoader, CDI11Bootstrap bootstrap) {
                    try {
                        return new Weld11TestUrlDeployment(resourceLoader, bootstrap, testConfig);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            container = weld.initialize();
        }
        catch (ClassFormatError e) {
            throw parseClassFormatError(e);
        }

        return container.select(testClass).get();
    }

    @Override
    public void afterEach(ExtensionContext context)
    {
        weld.shutdown();
    }

    private TestConfiguration createTestConfiguration(Class<?> clazz, Method testMethod) {
        return new TestConfiguration(clazz, testMethod);
    }

    private static ClassFormatError parseClassFormatError(ClassFormatError e) {
        if (e.getMessage().startsWith(ABSENT_CODE_PREFIX)) {
            String offendingClass = e.getMessage().substring(ABSENT_CODE_PREFIX.length());
            URL url = CdiUnitExtension.class.getClassLoader().getResource(offendingClass + ".class");

            assert url != null;

            return new ClassFormatError("'" + offendingClass.replace('/', '.')
                + "' is an API only class. You need to remove '"
                + url.toString().substring(9, url.toString().indexOf("!")) + "' from your classpath");
        } else {
            return e;
        }
    }
}
