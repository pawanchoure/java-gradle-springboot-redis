package com.pawan.choure.jedisexamples.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class RedisService {


    @Cacheable(value = "messageCache", condition = "'guitar'.equals(#instrument)")
    public String play(final String instrument) {
        System.out.println("Executing: " + this.getClass().getSimpleName() + ".play(" + instrument + ")");
        return "paying " + instrument + "!";
    }

    @Cacheable(value = "instrument",key = "#instrument")
    public String getInstrument(String instrument) {
       // System.out.println("Executing: " + this.getClass().getSimpleName() + ".play(" + instrument + ")");
        return instrument;
    }
}

