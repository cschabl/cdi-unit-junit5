package com.github.cschabl.cdiunit.junit5;

import com.github.cschabl.cdiunit.junit5.beans.ServiceBean;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(CdiUnitExtension.class)
class CdiUnitExtensionPerClassLifecycleTest {

    @Inject
    private ServiceBean serviceBean;

    private int beanInstanceHashCode;

    @BeforeAll
    void beforeAll() {
        beanInstanceHashCode = serviceBean.hashCode();
    }

    @Order(1)
    @Test
    void firstTest()
    {
        assertEquals(serviceBean.hashCode(), beanInstanceHashCode);
    }

    @Disabled
    @Order(2)
    @Test
    void secondTest()
    {
        fail();
    }

    @Order(3)
    @Test
    void thirdTest()
    {
        assertEquals(serviceBean.hashCode(), beanInstanceHashCode);
    }
}
