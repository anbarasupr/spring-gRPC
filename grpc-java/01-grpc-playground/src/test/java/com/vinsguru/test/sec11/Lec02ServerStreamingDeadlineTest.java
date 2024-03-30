package com.vinsguru.test.sec11;

import com.google.common.util.concurrent.Uninterruptibles;
import com.vinsguru.models.sec11.Money;
import com.vinsguru.models.sec11.WithdrawRequest;
import com.vinsguru.test.common.ResponseObserver;
import io.grpc.Deadline;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Lec02ServerStreamingDeadlineTest extends AbstractTest {
    private static final Logger log = LoggerFactory.getLogger(Lec01UnaryDeadlineTest.class);
	
    @Test
    public void blockingDeadlineTestStreamDataStill() {
        try {		
            var request = WithdrawRequest.newBuilder()
                                         .setAccountNumber(1)
                                         .setAmount(50) //emit 10 dollars one by one and take 5 seconds to complete
                                         .build();
            
            // After Deadline, Deadline exception is thrown but the server will stream remaining values but the client wont accept after the deadline
            // This will be issue for some cases
            var iterator = this.bankBlockingStub
                    .withDeadline(Deadline.after(2, TimeUnit.SECONDS)) // deadline for whole rpc call
                    .withdraw(request);
            while (iterator.hasNext()) {
            	log.info("{}", iterator.next());
             }        	
		} catch (Exception e) {
			// e.printStackTrace();
        	log.error(e.getMessage());
		}
        Uninterruptibles.sleepUninterruptibly(7, TimeUnit.SECONDS);
     }
    
    
    @Test
    public void blockingDeadlineTest() {
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var request = WithdrawRequest.newBuilder()
                                         .setAccountNumber(1)
                                         .setAmount(50) //emit 10 dollars one by one and take 5 seconds to complete
                                         .build();
            
            var iterator = this.bankBlockingStub
                    .withDeadline(Deadline.after(2, TimeUnit.SECONDS)) // deadline for whole rpc call
                    .withdraw(request);
            while (iterator.hasNext()) {
                iterator.next(); // we do not care about the response.
            }
        });
        Assertions.assertEquals(Status.Code.DEADLINE_EXCEEDED, ex.getStatus().getCode());
    }

    @Test
    public void asyncDeadlineTest() {
        var observer = ResponseObserver.<Money>create();
        var request = WithdrawRequest.newBuilder()
                                     .setAccountNumber(1)
                                     .setAmount(50)
                                     .build();
        this.bankStub
                .withDeadline(Deadline.after(2, TimeUnit.SECONDS))
                .withdraw(request, observer);
        observer.await();
        Assertions.assertEquals(2, observer.getItems().size()); // we would have received 2 items
        Assertions.assertEquals(Status.Code.DEADLINE_EXCEEDED, Status.fromThrowable(observer.getThrowable()).getCode());
    }

}
