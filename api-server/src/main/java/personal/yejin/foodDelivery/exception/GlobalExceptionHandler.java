package personal.yejin.foodDelivery.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static personal.yejin.foodDelivery.exception.ErrorCode.PAYMENT_REQUEST_FAIL;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException e) {
        log.error("결제 처리 실패: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(PAYMENT_REQUEST_FAIL.code, PAYMENT_REQUEST_FAIL.message));
    }

}
