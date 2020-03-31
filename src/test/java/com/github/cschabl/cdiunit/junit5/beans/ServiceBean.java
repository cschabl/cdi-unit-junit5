package com.github.cschabl.cdiunit.junit5.beans;

import javax.inject.Inject;

public class ServiceBean {

    @Inject
    private GreetingPrefixBean prefixProvider;

    public String sayHello(String name)
    {
        return prefixProvider.getGreetingPrefix() + name;
    }
}
