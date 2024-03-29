package com.vinsguru.sec03;

import com.vinsguru.models.sec03.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lec07DefaultValues {

    private static final Logger log = LoggerFactory.getLogger(Lec07DefaultValues.class);

    public static void main(String[] args) {

        var school = School.newBuilder().build();

        log.info("{}", school.getId());
        log.info("{}", school.getName());
        log.info("{}", school.getAddress().getCity());

        // Proto craetes defaultInstance for every class type and it knows the default values for string, int ...
        // For Enum, to define the default value, the zero th index in proto file is considered as default value
        log.info("is default? : {}", school.getAddress().equals(Address.getDefaultInstance()));

        // has
        log.info("has address? {}", school.hasAddress());

        // collection
        var lib = Library.newBuilder().build();
        log.info("{}", lib.getBooksList());

        // map
        var dealer = Dealer.newBuilder().build();
        log.info("{}", dealer.getInventoryMap());

        // enum
        var car = Car.newBuilder().build();
        log.info("{}", car.getBodyStyle());

    }

}
