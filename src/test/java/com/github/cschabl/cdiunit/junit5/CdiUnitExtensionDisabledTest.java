package com.github.cschabl.cdiunit.junit5;

import com.github.cschabl.cdiunit.junit5.beans.ServiceBean;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Actually, a test driver only. Don't know how to assert that the extension shuts down a running Weld container of
 * a disabled test.
 */
@ExtendWith(CdiUnitExtension.class)
class CdiUnitExtensionDisabledTest {

    @Inject
    private ServiceBean serviceBean;

    @Test
    void enabledTest() {
        serviceBean.sayHello("Joe");
    }

    @Disabled
    @Test
    void disabledTest() {
        fail();
    }

    @Test
    void anotherEnabledTest() {
        serviceBean.sayHello("Doe");
    }
}
