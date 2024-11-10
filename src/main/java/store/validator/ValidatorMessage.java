package store.validator;

public enum ValidatorMessage {
    ERROR("[ERROR]"),
    FILE_NOT_FOUND(ERROR.getErrorMessage() + " 상품 파일을 찾을 수 없습니다. 올바른 경로에 위치하였는지 확인해 주세요."),
    FILE_WRONG_FORMAT(ERROR.getErrorMessage() + " 파일 내용 형식이 올바르지 않습니다. 파일 내용을 확인해 주세요."),
    WRONG_BUY_FORMAT(ERROR.getErrorMessage() + " 올바르지 않은 형식으로 입력했습니다. 다시 입력해 주세요."),
    NO_PRODUCT(ERROR.getErrorMessage() + " 존재하지 않는 상품입니다. 다시 입력해 주세요."),
    BUY_PRODUCT_OVER_STOCK(ERROR.getErrorMessage() + " 재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요."),
    NOT_YES_OR_NO(ERROR.getErrorMessage() + " 잘못된 입력입니다. 다시 입력해 주세요.");

    private final String errorMessage;

    ValidatorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
