package com.vinsguru.test.sec11;

import com.vinsguru.models.sec06.AccountBalance;
import com.vinsguru.models.sec06.BalanceCheckRequest;
import com.vinsguru.models.sec06.BankServiceGrpc;
import com.vinsguru.models.sec06.DepositRequest;
import com.vinsguru.models.sec06.Money;
import com.vinsguru.test.common.ResponseObserver;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
    It is a class to demo nginx load balancing
    Ensure that you run the 2 bank service instance
    1. use sec06 bank service
    2. run 2 instances. 1 on port 6565 and other on 7575
    3. start nginx (src/test/resources). nginx listens on port 8585
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Lec07LoadBalancingDemoTest {

    private static final Logger log = LoggerFactory.getLogger(Lec07LoadBalancingDemoTest.class);
    private BankServiceGrpc.BankServiceBlockingStub bankBlockingStub;
    private BankServiceGrpc.BankServiceStub bankStub;
    private ManagedChannel channel;

    @BeforeAll
    public void setup() {
        this.channel = ManagedChannelBuilder.forAddress("localhost", 8585)
                                            .usePlaintext()
                                            .build();
        this.bankBlockingStub = BankServiceGrpc.newBlockingStub(channel);
        this.bankStub = BankServiceGrpc.newStub(channel);
    }

    // I do not want to run this as part of mvn test
    //@Test
    public void loadBalancingDemo() {
        for (int i = 1; i <= 10 ; i++) {
            var request = BalanceCheckRequest.newBuilder()
                                             .setAccountNumber(i)
                                             .build();
            var response = this.bankBlockingStub.getAccountBalance(request);
            log.info("{}", response);
        }
    }
    
    @Test
    public void depositTest() {
        var responseObserver = ResponseObserver.<AccountBalance>create();
        var requestObserver = this.bankStub.deposit(responseObserver);

        // initial message - account number
        requestObserver.onNext(DepositRequest.newBuilder().setAccountNumber(5).build());

        // sending stream of money
        IntStream.rangeClosed(1, 10)
                .mapToObj(i -> Money.newBuilder().setAmount(10).build())
                .map(m -> DepositRequest.newBuilder().setMoney(m).build())
                .forEach(requestObserver::onNext);

       // notifying the server that we are done
        requestObserver.onCompleted();

        // at this point out response observer should receive a response
        responseObserver.await();

        // assert
        Assertions.assertEquals(1, responseObserver.getItems().size());
        Assertions.assertEquals(200, responseObserver.getItems().getFirst().getBalance());
        Assertions.assertNull(responseObserver.getThrowable());
    }

    @AfterAll
    public void stop() {
        this.channel.shutdownNow();
    }

}
