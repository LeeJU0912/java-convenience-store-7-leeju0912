package store;

import store.controller.StoreController;
import store.controller.StoreControllerFactory;

public class Application {
    public static void main(String[] args) {
        // TODO: 프로그램 구현
        StoreController storeController = StoreControllerFactory.getStoreController();
        storeController.run();
    }
}
