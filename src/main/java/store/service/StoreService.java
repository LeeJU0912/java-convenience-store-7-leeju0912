package store.service;

import store.domain.BuyProduct;
import store.domain.Product;
import store.domain.Receipt;
import store.dto.BuyProducts;
import store.dto.Products;
import store.domain.Promotion;
import store.dto.PromotionProducts;
import store.dto.Promotions;
import store.validator.FileValidator;
import store.validator.ValidatorMessage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StoreService {

    private static final String PRODUCTS_FILE_PATH = "src/main/resources/products.md";
    private static final String PROMOTIONS_FILE_PATH = "src/main/resources/promotions.md";

    private Products products;
    private PromotionProducts promotionProducts;
    private BuyProducts buyProducts;
    private Promotions promotions;
    private Receipt receipt;

    public void createNewProducts() {
        products = new Products();
    }

    public void createNewPromotionProducts() {
        promotionProducts = new PromotionProducts();
    }

    public void createNewPromotions() {
        promotions = new Promotions();
    }

    public void createNewReceipt() {
        receipt = new Receipt();
    }

    public Products getProducts() {
        return products;
    }

    public PromotionProducts getPromotionProducts() {
        return promotionProducts;
    }

    public BuyProducts getBuyProducts() {
        return buyProducts;
    }

    public Promotions getPromotions() {
        return promotions;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void readProductData() {
        addProducts();
        addPromotions();
        addPromotionsToProducts();
    }

    private void addProducts() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(PRODUCTS_FILE_PATH));
            String product = reader.readLine();
            while((product = reader.readLine()) != null) {
                FileValidator.validateProductParsing(product);
                saveProductFromFile(product);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(ValidatorMessage.FILE_NOT_FOUND.getErrorMessage());
        }
    }

    private void saveProductFromFile(String product) {
        Product parsedProduct = parseProduct(product);
        if (parsedProduct.getPromotionName().equals("null")) {
            getProducts().addProduct(parsedProduct);
            return;
        }
        getPromotionProducts().addProduct(parsedProduct);
    }

    private Product parseProduct(String productData) {
        String[] productInfo = splitInput(productData);
        return new Product(productInfo[0], Long.parseLong(productInfo[1]), Long.parseLong(productInfo[2]), productInfo[3]);
    }

    private void addPromotions() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(PROMOTIONS_FILE_PATH));
            String promotion = reader.readLine();
            while((promotion = reader.readLine()) != null) {
                FileValidator.validatePromotionParsing(promotion);
                savePromotionFromFile(promotion);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(ValidatorMessage.FILE_NOT_FOUND.getErrorMessage());
        }
    }

    private void savePromotionFromFile(String promotion) {
        Promotion parsedPromotion = parsePromotion(promotion);
        getPromotions().addPromotion(parsedPromotion);
    }

    private Promotion parsePromotion(String promotion) {
        String[] promotionInfo = splitInput(promotion);
        LocalDateTime from = LocalDateTime.parse(promotionInfo[3] + "T00:00:00");
        LocalDateTime to = LocalDateTime.parse(promotionInfo[4] + "T23:59:59");
        return new Promotion(promotionInfo[0], Long.parseLong(promotionInfo[1]), Long.parseLong(promotionInfo[2]), from, to);
    }

    private void addPromotionsToProducts() {
        for (Promotion promotion : getPromotions().getPromotions()) {
            getPromotionProducts().getProducts().values()
                    .stream()
                    .filter(product -> product.getPromotionName().equals(promotion.promoteDescription()))
                    .forEach(product -> product.setPromotion(promotion));
        }
    }

    public void setBuyProducts(String input) {
        buyProducts = parseBuyProducts(input);
    }

    private BuyProducts parseBuyProducts(String input) {
        String[] buyProductsInfo = splitInput(input);
        return new BuyProducts(convertToBuyProducts(buyProductsInfo));
    }

    private Map<String, BuyProduct> convertToBuyProducts(String[] buyProductsInfo) {
        Map<String, BuyProduct> parsedBuyProducts = new ConcurrentHashMap<>();
        for (String buyProductInfo : buyProductsInfo) {
            String[] splitBuyProductInfo = splitBuyProductInfo(buyProductInfo);
            parsedBuyProducts.put(splitBuyProductInfo[0], convertToBuyProduct(splitBuyProductInfo));
        }
        return parsedBuyProducts;
    }

    private BuyProduct convertToBuyProduct(String[] splitBuyProductInfo) {
        String buyProductName = splitBuyProductInfo[0];
        Long buyProductQuantity = Long.parseLong(splitBuyProductInfo[1]);
        Long productPrice = getProductPrice(buyProductName);
        return new BuyProduct(buyProductName, buyProductQuantity, productPrice);
    }

    private Long getProductPrice(String buyProductName) {
        Long productPrice = getProducts().getProductPrice(buyProductName);
        if (productPrice == null) {
            productPrice = getPromotionProducts().getProductPrice(buyProductName);
        }
        return productPrice;
    }

    private static String[] splitBuyProductInfo(String buyProductInfo) {
        buyProductInfo = buyProductInfo.replaceAll("[\\[\\]]", "");
        return Arrays.stream(buyProductInfo.split("-")).filter(productInfo -> !productInfo.isEmpty()).toArray(String[]::new);
    }

    private String[] splitInput(String input) {
        return Arrays.stream(input.split(",")).filter(productInfo -> !productInfo.isEmpty()).toArray(String[]::new);
    }

    public Long calculateSameProductStockQuantity(BuyProduct buyProduct) {
        return getProducts().getProductQuantity(buyProduct.getName());
    }

    public Long calculatePromotionOnlyProductQuantity(BuyProduct buyProduct) {
        return getPromotionProducts().getPromotionOnlyProductQuantity(buyProduct);
    }

    public boolean checkPromotionProductStock(String productName) {
        Long promotionProductStockQuantity = getPromotionProducts().getPromotionProductStockQuantity(productName);
        if (promotionProductStockQuantity < getPromotionProducts().getPromotionValidQuantity(productName)) {
            return false;
        }
        return true;
    }

    public void buyProcessNotPromotion(String productName) {
        calculateStockPromotionPart(productName);
        calculateStockNotPromotionPart(productName);
    }

    private void calculateStockPromotionPart(String productName) {
        Product promotionProduct = getPromotionProducts().getProductByName(productName);
        if (promotionProduct == null) {
            return;
        }
        BuyProduct buyProduct = getBuyProducts().buyProducts().get(productName);
        Long nowQuantity = buyProduct.getQuantity();
        Long nowPromotionProductStock = promotionProduct.getStock();

        buyAndReduceStockFromPromotionProduct(nowQuantity, nowPromotionProductStock, promotionProduct);
    }

    private void buyAndReduceStockFromPromotionProduct(Long nowQuantity, Long nowPromotionProductStock, Product promotionProduct) {
        if (nowQuantity <= nowPromotionProductStock) {
            buyAndReduceFromPromotionProductWithQuantity(nowQuantity, promotionProduct);
            return;
        }
        buyAndReduceFromPromotionProductWithQuantity(nowPromotionProductStock, promotionProduct);
    }

    private void buyAndReduceFromPromotionProductWithQuantity(Long nowQuantity, Product promotionProduct) {
        reducePromotionProductQuantity(nowQuantity, promotionProduct);
        addReceiptBuyProductQuantity(nowQuantity, promotionProduct);
        reduceBuyProductQuantity(nowQuantity, promotionProduct);
    }

    private void calculateStockNotPromotionPart(String productName) {
        BuyProduct buyProduct = getBuyProducts().buyProducts().get(productName);
        Long nowQuantity = buyProduct.getQuantity();

        Product product = getProducts().getProductByName(productName);
        Long productStock = product.getStock();
        buyAndReduceStockFromProduct(productStock, product, nowQuantity);
    }

    private void buyAndReduceStockFromProduct(Long productStock, Product product, Long nowQuantity) {
        reduceProductQuantity(productStock, product);
        addReceiptBuyProductQuantity(nowQuantity, product);
        reduceBuyProductQuantity(nowQuantity, product);
    }

    private void reducePromotionProductQuantity(Long quantity, Product product) {
        product.reduceStock(quantity);
    }

    private void reduceProductQuantity(Long quantity, Product product) {
        product.reduceStock(quantity);
    }

    private void reduceBuyProductQuantity(Long quantity, Product product) {
        getBuyProducts().buyProducts().get(product.getName()).reduceQuantity(quantity);
    }

    private void addReceiptBuyProductQuantity(Long quantity, Product product) {
        getReceipt().addBuyProductQuantity(product, quantity);
    }

    public void setMembershipDiscount() {
        getReceipt().setMembershipDiscount();
    }

    public void buyPromotionForFree(String productName, Long promotionQuantity) {
        getPromotionProducts().reducePromotionQuantity(productName, promotionQuantity);
    }


    public void validateInputBuyProducts(String input) {
        String[] buyProductsInfo = splitInput(input);

        for (String buyProductInfo : buyProductsInfo) {
            String[] splitBuyProductInfo = splitBuyProductInfo(buyProductInfo);
            checkBuyProductInfoSize(splitBuyProductInfo);
            Long buyProductQuantity = checkConvertBuyProductQuantity(splitBuyProductInfo);
            String buyProductName = checkValidBuyProductName(splitBuyProductInfo);
            checkBuyProductQuantityOverStock(buyProductName, buyProductQuantity);
        }
    }

    private static void checkBuyProductInfoSize(String[] splitBuyProductInfo) {
        if (splitBuyProductInfo.length != 2) {
            throw new IllegalArgumentException(ValidatorMessage.WRONG_BUY_FORMAT.getErrorMessage());
        }
    }

    private static Long checkConvertBuyProductQuantity(String[] splitBuyProductInfo) {
        try {
            return Long.parseLong(splitBuyProductInfo[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ValidatorMessage.WRONG_BUY_FORMAT.getErrorMessage());
        }
    }

    private String checkValidBuyProductName(String[] splitBuyProductInfo) {
        String buyProductName = splitBuyProductInfo[0];
        if (!getPromotionProducts().getProducts().containsKey(buyProductName)
                && !getProducts().getProducts().containsKey(buyProductName)) {
            throw new IllegalArgumentException(ValidatorMessage.NO_PRODUCT.getErrorMessage());
        }
        return buyProductName;
    }

    private void checkBuyProductQuantityOverStock(String buyProductName, Long buyProductQuantity) {
        Product product = getPromotionProducts().getProducts().get(buyProductName);
        if (product == null) {
            product = getProducts().getProducts().get(buyProductName);
        }
        Long productQuantity = product.getStock();

        if (buyProductQuantity > productQuantity) {
            throw new IllegalArgumentException(ValidatorMessage.BUY_PRODUCT_OVER_STOCK.getErrorMessage());
        }
    }

    public void validateInputYesOrNo(String input) {
        if (!input.equals("Y") && !input.equals("N")) {
            throw new IllegalArgumentException(ValidatorMessage.NOT_YES_OR_NO.getErrorMessage());
        }
    }
}