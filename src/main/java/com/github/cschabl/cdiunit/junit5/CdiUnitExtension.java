package com.github.cschabl.cdiunit.junit5;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.resources.spi.ResourceLoader;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.Weld11TestUrlDeployment;
import io.github.cdiunit.internal.WeldTestUrlDeployment;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * CdiUnitExtension is a JUnit5 test instance factory that uses a CDI container to create unit test objects.
 * Simply add <code>&#064;ExtendWith(CdiUnitExtension.class)</code> to your test class.
 * This extension supports JUnit 5's PER_CLASS lifecycle (see {@code TestInstance.Lifecycle.PER_CLASS}).
 *
 * <pre>
 *   <code>
 *
 *  &#064;ExtendWith(CdiUnitExtension.class) // Runs the test with CDI-Unit
 *  class MyTest {
 *     &#064;Inject
 *     MyBean beanUnderTest; // This will be injected before the tests are run!
 *
 *     ... //The rest of the test goes here.
 *   }
 *   </code>
 * </pre>
 */
public class CdiUnitExtension implements TestInstanceFactory, AfterEachCallback, TestWatcher, AfterAllCallback,
        TestInstancePostProcessor
{
    private static final Logger logger = Logger.getLogger(CdiUnitExtension.class.getName());

    private static final String ABSENT_CODE_PREFIX = "Absent Code attribute in method that is not native or abstract in class file ";
    private static final String JNDI_FACTORY_PROPERTY = "java.naming.factory.initial";

    private TestInstance.Lifecycle lifecycle;

    private Weld weld;
    private WeldContainer container;
    private InitialContext initialContext;
    private String oldFactory;

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext)
        throws TestInstantiationException
    {
        Class<?> testClass = extensionContext.getTestClass()
            .orElseThrow(() -> new TestInstantiationException("test class required"));

        logger.fine(() -> "testClass=" + testClass.getName() + ", lifecycle=" + extensionContext.getTestInstanceLifecycle());

        lifecycle = extensionContext.getTestInstanceLifecycle().orElse(TestInstance.Lifecycle.PER_METHOD);

        if (container != null && container.isRunning()) {
            logger.warning("previous container still running");
        }

        try {
            long start = System.currentTimeMillis();
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

                // override for Weld 1.x
                @SuppressWarnings("unused")
                protected Deployment createDeployment(ResourceLoader resourceLoader, Bootstrap bootstrap) {
                    try {
                        return new WeldTestUrlDeployment(resourceLoader, bootstrap, testConfig);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            setJndiFactoryProperty();

            container = weld.initialize();
            logger.fine(() -> "Initialization of container took " + (System.currentTimeMillis() - start) + " ms");
        }
        catch (ClassFormatError e) {
            throw parseClassFormatError(e);
        }

        return container.select(testClass).get();
    }

    @Override
    public void postProcessTestInstance(Object o, ExtensionContext extensionContext) throws Exception {
        logger.finer(() -> "testClass=" + extensionContext.getTestClass());

        bindBeanManagerToInitialContext();
    }

    @Override
    public void afterEach(ExtensionContext context) throws NamingException {
        logger.finer(() -> "testMethod=" + context.getRequiredTestMethod().getName());

        if (lifecycle == TestInstance.Lifecycle.PER_METHOD) {
            shutdownWeld();
        }
    }

    @Override
    public void testDisabled(ExtensionContext extensionContext, Optional<String> optional) {
        logger.finer(() -> "testMethod=" + extensionContext.getRequiredTestMethod().getName());

        if (lifecycle == TestInstance.Lifecycle.PER_METHOD) {
            try {
                shutdownWeld();
            } catch (NamingException e) {
                logger.warning("testDisabled" + e.getMessage());
            }
        }
    }

    @Override
    public void testSuccessful(ExtensionContext extensionContext) {
    }

    @Override
    public void testAborted(ExtensionContext extensionContext, Throwable throwable) {
    }

    @Override
    public void testFailed(ExtensionContext extensionContext, Throwable throwable) {
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        if (lifecycle == TestInstance.Lifecycle.PER_CLASS) {
            shutdownWeld();
        }
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

    private void setJndiFactoryProperty(){
        oldFactory = System.getProperty(JNDI_FACTORY_PROPERTY);

        if (oldFactory == null) {
            System.setProperty(JNDI_FACTORY_PROPERTY, "io.github.cdiunit.internal.naming.CdiUnitContextFactory");
            logger.finer(() -> "set CdiUnitContextFactory as JNDI factory");
        }
        else {
            logger.finer(() -> "JNDI factory already set to " + oldFactory);
        }
    }

    private void bindBeanManagerToInitialContext() throws NamingException {
        initialContext = new InitialContext();
        initialContext.bind("java:comp/BeanManager", container.getBeanManager());
    }

    private void shutdownWeld() throws NamingException {
        initialContext.close();
        weld.shutdown();

        if (oldFactory != null) {
            System.setProperty(JNDI_FACTORY_PROPERTY, oldFactory);
        } else {
            System.clearProperty(JNDI_FACTORY_PROPERTY);
        }
    }
}
