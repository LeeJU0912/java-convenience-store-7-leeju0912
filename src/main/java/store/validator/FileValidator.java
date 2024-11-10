package store.validator;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

public class FileValidator {

    public static void validateProductParsing(String fileDataProduct) {
        String[] splitData = Arrays.stream(fileDataProduct.split(",")).filter(productInfo -> !productInfo.isEmpty()).toArray(String[]::new);
        checkFileDataSize(splitData);
        checkFileDataLongConvert(splitData);
    }

    private static void checkFileDataSize(String[] splitData) {
        if (splitData.length != 4) {
            throw new IllegalArgumentException(ValidatorMessage.FILE_WRONG_FORMAT.getErrorMessage());
        }
    }

    public static void validatePromotionParsing(String fileDataProduct) {
        String[] splitData = Arrays.stream(fileDataProduct.split(",")).filter(productInfo -> !productInfo.isEmpty()).toArray(String[]::new);
        checkFileDataLongConvert(splitData);

        checkFileDataLocalDateTimeConvert(splitData);
    }

    private static void checkFileDataLongConvert(String[] splitData) {
        try {
            Long.parseLong(splitData[1]);
            Long.parseLong(splitData[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ValidatorMessage.FILE_WRONG_FORMAT.getErrorMessage());
        }
    }

    private static void checkFileDataLocalDateTimeConvert(String[] splitData) {
        try {
            LocalDateTime.parse(splitData[3] + "T00:00:00");
            LocalDateTime.parse(splitData[4] + "T23:59:59");
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(ValidatorMessage.FILE_WRONG_FORMAT.getErrorMessage());
        }
    }
}
