package com.vinsguru.sec06;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vinsguru.models.sec06.BalanceCheckRequestV1;
import com.vinsguru.models.sec06.BankServiceV1Grpc;

import io.grpc.ManagedChannelBuilder;

public class GrpcClient {
	private static final Logger log = LoggerFactory.getLogger(GrpcClient.class);

	public static void main(String[] args) {
		// disable TLS by usePlainText()
		var channel = ManagedChannelBuilder.forAddress("localhost", 6565).usePlaintext().build();
		var stub = BankServiceV1Grpc.newBlockingStub(channel);
		var balance = stub.getAccountBalance(BalanceCheckRequestV1.newBuilder().setAccountNumber(1).build());
		log.info("balance {}", balance);
	}
}
