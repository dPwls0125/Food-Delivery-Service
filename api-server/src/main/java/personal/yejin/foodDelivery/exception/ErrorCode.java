package personal.yejin.foodDelivery.exception;

public enum ErrorCode {

    PAYMENT_REQUEST_FAIL("payment-1","결제 요청에 실패했습니다.");

    String code;
    String message;

    ErrorCode(String code, String errorMessage) {
        this.code = code;
        this.message = errorMessage;
    }
}
