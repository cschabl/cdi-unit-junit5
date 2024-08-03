package com.github.cschabl.cdiunit.junit5.beans;

import jakarta.inject.Inject;

public class ServiceBean {

    @Inject
    private GreetingPrefixBean prefixProvider;

    public String sayHello(String name)
    {
        return prefixProvider.getGreetingPrefix() + name;
    }
}
