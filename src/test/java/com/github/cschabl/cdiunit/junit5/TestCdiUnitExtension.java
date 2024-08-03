package com.github.cschabl.cdiunit.junit5;

import com.github.cschabl.cdiunit.junit5.beans.AImplementation1;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(CdiUnitExtension.class)
class TestCdiUnitExtension extends BaseTest {

    @Inject
    private AImplementation1 aImpl;

    @Inject
    private BeanManager beanManager;

    @Test
    void testBeanManager() {
        assertNotNull(getBeanManager());
        assertNotNull(beanManager);
    }

    @Test
    public void testSuper() {
        assertNotNull(aImpl.getBeanManager());
    }
}
