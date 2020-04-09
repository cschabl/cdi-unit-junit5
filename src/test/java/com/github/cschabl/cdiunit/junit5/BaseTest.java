package com.github.cschabl.cdiunit.junit5;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

class BaseTest
{
    @Inject
    private BeanManager beanManager;

    public BeanManager getBeanManager() {
        return beanManager;
    }
}
