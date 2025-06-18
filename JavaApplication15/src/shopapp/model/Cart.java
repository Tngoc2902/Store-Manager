package shopapp.model;

import shopapp.observer.CartObserver;
import java.util.*;

public class Cart {
    private static Cart instance;
    private final List<CartItem> items = new ArrayList<>();
    private final List<CartObserver> observers = new ArrayList<>();

    private Cart() {}

    public static Cart getInstance() {
        if (instance == null) {
            instance = new Cart();
        }
        return instance;
    }

    public void addItem(Product product, int quantity) {
        for (CartItem item : items) {
            if (item.getProduct().getId() == product.getId()) {
                item.setQuantity(item.getQuantity() + quantity);
                notifyObservers();
                return;
            }
        }
        items.add(new CartItem(product, quantity));
        notifyObservers();
    }

    public void removeItem(Product product) {
        items.removeIf(item -> item.getProduct().getId() == product.getId());
        notifyObservers();
    }

    public void clear() {
        items.clear();
        notifyObservers();
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items); // tránh bị sửa trực tiếp
    }

    // Observer methods
    public void addObserver(CartObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(CartObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (CartObserver observer : observers) {
            observer.onCartUpdated();
        }
    }
    public void updateQuantity(int index, int quantity) {
    if (index >= 0 && index < items.size()) {
        items.get(index).setQuantity(quantity);
    }
}

    
    
    public double getTotal() {
    double total = 0;
    for (CartItem item : items) {
        total += item.getProduct().getPrice() * item.getQuantity();
    }
    return total;
    }

}
