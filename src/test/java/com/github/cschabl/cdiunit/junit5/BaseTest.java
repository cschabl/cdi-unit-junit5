package com.github.cschabl.cdiunit.junit5;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

class BaseTest
{
    @Inject
    private BeanManager beanManager;

    public BeanManager getBeanManager() {
        return beanManager;
    }
}
