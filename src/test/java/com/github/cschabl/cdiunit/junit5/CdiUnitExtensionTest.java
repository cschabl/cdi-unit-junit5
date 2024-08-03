package com.github.cschabl.cdiunit.junit5;

import com.github.cschabl.cdiunit.junit5.beans.ServiceBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(CdiUnitExtension.class)
class CdiUnitExtensionTest {

    @Inject
    private ServiceBean serviceBean;

    @Test
    void shouldInjectServiceBean()
    {
        String givenName = "JUnit 5";
        String actualHelloMessage = serviceBean.sayHello(givenName);

        assertTrue(actualHelloMessage.contains(givenName), () -> "helloMessage.contains('" + givenName + "')");
    }
}
