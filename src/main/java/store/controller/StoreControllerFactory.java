package store.controller;

import store.service.StoreService;

public class StoreControllerFactory {
    public static StoreController getStoreController() {
        return new StoreController(new StoreService());
    }
}
