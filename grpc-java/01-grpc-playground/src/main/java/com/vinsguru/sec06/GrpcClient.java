package com.vinsguru.sec06;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vinsguru.models.sec06.AccountBalanceV1;
import com.vinsguru.models.sec06.BalanceCheckRequestV1;
import com.vinsguru.models.sec06.BankServiceV1Grpc;

import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class GrpcClient {
	private static final Logger log = LoggerFactory.getLogger(GrpcClient.class);

	public static void main(String[] args) throws InterruptedException {
		// blockingStubClientCall();
		nonBlockingStubClientCall();
	}

	public static void blockingStubClientCall() {
		// disable TLS by usePlainText()
		var channel = ManagedChannelBuilder.forAddress("localhost", 6565).usePlaintext().build();
		var stub = BankServiceV1Grpc.newBlockingStub(channel);
		var balance = stub.getAccountBalance(BalanceCheckRequestV1.newBuilder().setAccountNumber(1).build());
		log.info("balance {}", balance);
	}

	public static void nonBlockingStubClientCall() throws InterruptedException {
		// disable TLS by usePlainText()
		var channel = ManagedChannelBuilder.forAddress("localhost", 6565).usePlaintext().build();
		var stub = BankServiceV1Grpc.newStub(channel); // supports all 4 types of communication patterns
		StreamObserver<AccountBalanceV1> streamObserver = new StreamObserver<AccountBalanceV1>() {
			@Override
			public void onNext(AccountBalanceV1 value) {
				log.info("response balance {}", value);
			}

			@Override
			public void onError(Throwable t) {
				log.info("error balance {}", t);
			}

			@Override
			public void onCompleted() {
				log.info("response balance completed");
			}
		};

		stub.getAccountBalance(BalanceCheckRequestV1.newBuilder().setAccountNumber(1).build(), streamObserver);

		log.info("Triggered");
		Thread.sleep(5000);
	}
}
