package com.vinsguru.sec06;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vinsguru.models.sec06.AccountBalanceV1;
import com.vinsguru.models.sec06.BalanceCheckRequestV1;
import com.vinsguru.models.sec06.BankServiceV1Grpc;
import com.vinsguru.sec06.repository.AccountRepository;

import io.grpc.stub.StreamObserver;

public class BankServiceV1 extends BankServiceV1Grpc.BankServiceV1ImplBase {
 
    private static final Logger log = LoggerFactory.getLogger(BankServiceV1.class);

    @Override
    public void getAccountBalance(BalanceCheckRequestV1 request, StreamObserver<AccountBalanceV1> responseObserver) {
        log.info("request received {}", request.getAccountNumber());
        var accountNumber = request.getAccountNumber();
        var balance = AccountRepository.getBalance(accountNumber);
        var accountBalance = AccountBalanceV1.newBuilder()
                .setAccountNumber(accountNumber)
                .setBalance(balance)
                .build();
        responseObserver.onNext(accountBalance);
        responseObserver.onCompleted();
    }
}
