package com.vinsguru.sec06;

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.protobuf.Empty;
import com.vinsguru.models.sec06.*;
import com.vinsguru.sec06.repository.AccountRepository;
import com.vinsguru.sec06.requesthandlers.DepositRequestHandler;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class BankService extends BankServiceGrpc.BankServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(BankService.class);

    // Unary model - typical client server interaction as one time
    @Override
    public void getAccountBalance(BalanceCheckRequest request, StreamObserver<AccountBalance> responseObserver) {
        log.info("request received {}", request.getAccountNumber());
        var accountNumber = request.getAccountNumber();
        var balance = AccountRepository.getBalance(accountNumber);
        var accountBalance = AccountBalance.newBuilder()
                .setAccountNumber(accountNumber)
                .setBalance(balance)
                .build();
        responseObserver.onNext(accountBalance);
        responseObserver.onCompleted();
    }

    // Unary model - typical client server interaction as one time
    @Override
    public void getAllAccounts(Empty request, StreamObserver<AllAccountsResponse> responseObserver) {
        var accounts = AccountRepository.getAllAccounts()
                .entrySet()
                .stream()
                .map(e -> AccountBalance.newBuilder().setAccountNumber(e.getKey()).setBalance(e.getValue()).build())
                .toList();
        var response = AllAccountsResponse.newBuilder().addAllAccounts(accounts).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // SSE: Server Streaming - Deducts 10 denominations for the Withdrawal request and emits each items to client
    @Override
    public void withdraw(WithdrawRequest request, StreamObserver<Money> responseObserver) {
        /*
            Ideally we should do some input validation. But we are going to assume only happy path scenarios.
            Because, in gRPC, there are multiple ways to communicate the error message to the client. It has to be discussed in detail separately.
            Assumption: account # 1 - 10 & withdraw amount is multiple of $10
         */
        var accountNumber = request.getAccountNumber();
        var requestedAmount = request.getAmount();
        var accountBalance = AccountRepository.getBalance(accountNumber);
        log.info("Total balance available {}", accountBalance);
        if(requestedAmount > accountBalance){
            responseObserver.onCompleted(); // we will change it to proper error later
            return;
        }

        for (int i = 0; i < (requestedAmount / 10); i++) {
            var money = Money.newBuilder().setAmount(10).build();
            responseObserver.onNext(money);
            log.info("money withdrawn {}", money);
            AccountRepository.deductAmount(accountNumber, 10);
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        }
        accountBalance = AccountRepository.getBalance(accountNumber);
        log.info("Final balance available {}", accountBalance);
        responseObserver.onCompleted();
    }

    // Both Client and Server Streaming in Bi-directional like Websocket
    @Override
    public StreamObserver<DepositRequest> deposit(StreamObserver<AccountBalance> responseObserver) {
        return new DepositRequestHandler(responseObserver);
    }
}
