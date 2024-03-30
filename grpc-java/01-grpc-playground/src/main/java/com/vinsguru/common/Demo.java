package com.vinsguru.common;

import com.vinsguru.sec06.BankService;
import com.vinsguru.sec06.BankServiceV1;
import com.vinsguru.sec06.TransferService;
import com.vinsguru.sec07.FlowControlService;
import com.vinsguru.sec12.interceptors.ApiKeyValidationInterceptor;

/*
    a simple class to start the server with specific services for demo purposes
 */
public class Demo {

    public static void main(String[] args) {

        GrpcServer.create(6565, builder -> {
//                      builder.addService(new BankServiceV1());
//                      builder.addService(new BankService());
//                      builder.addService(new TransferService());
//                      builder.addService(new FlowControlService());
//        				builder.addService(new com.vinsguru.sec09.BankService());
            
                      // builder.addService(new com.vinsguru.sec10.BankService());
                      builder.addService(new com.vinsguru.sec12.BankService())
                      .intercept(new ApiKeyValidationInterceptor());
                   })
        
                  .start()
                  .await();

    }

//    public static void main(String[] args) {
//
//        GrpcServer.create(6565, builder -> {
//                      builder.addService(new BankServiceV1())
//                             .intercept(new ApiKeyValidationInterceptor());
//                  })
//                  .start()
//                  .await();
//
//    }
    
    
    /*  Created for load balancing demo
    private static class BankInstance1 {
        public static void main(String[] args) {
            GrpcServer.create(6565, new BankService())
                      .start()
                      .await();
        }
    }

    private static class BankInstance2 {
        public static void main(String[] args) {
            GrpcServer.create(7575, new BankService())
                      .start()
                      .await();
        }
    }
    */
}
