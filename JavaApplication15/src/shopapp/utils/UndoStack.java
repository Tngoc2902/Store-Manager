// UndoStack.java - Hỗ trợ hoàn tác thao tác sản phẩm
package shopapp.utils;

import shopapp.model.Product;
import java.util.List;
import java.util.Stack;

public class UndoStack {
    private static Stack<List<Product>> undoStack = new Stack<>();

    public static void push(List<Product> products) {
        // Tạo bản sao để tránh bị thay đổi ngoài ý muốn
        undoStack.push(List.copyOf(products));
    }

    public static List<Product> pop() {
        if (!undoStack.isEmpty()) {
            return undoStack.pop();
        }
        return null;
    }

    public static List<Product> peek() {
        if (!undoStack.isEmpty()) {
            return undoStack.peek();
        }
        return null;
    }

    public static int size() {
        return undoStack.size();
    }

    public static void clear() {
        undoStack.clear();
    }
}
