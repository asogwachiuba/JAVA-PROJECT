package Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Models.CartProduct;
import Models.Order;
import Models.Product;
import Models.ProductOrdered;
import Models.SessionTracker;

public class CustomerDB {
	
	private static Connection connect() throws SQLException {
		return DriverManager.getConnection(DatabaseConstants.url + DatabaseConstants.dbName, DatabaseConstants.username,
				DatabaseConstants.password);
	}
	
	
	 /**
     * Retrieves all products from the database.
     * 
     * @return a list of Product objects
     * @throws SQLException if a database access error occurs
     */
    public static List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String query = "SELECT id, name, measuring_format, qty_available, weight_per_unit FROM products";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String measuringFormat = resultSet.getString("measuring_format");
                int quantityAvailable = resultSet.getInt("qty_available");
                double weightPerUnit = resultSet.getDouble("weight_per_unit");

                Product product = new Product(id, name, measuringFormat, quantityAvailable, weightPerUnit);
                products.add(product);
            }
        }
        return products;
    }


    /**
     * Places an order in the database, adding to the orders table and linking products in ProductsOrdered.
     *
     * @param customerId      the ID of the customer making the order
     * @param cartProducts    the list of products in the cart with their quantities
     * @param deliveryDate    the delivery date of the order
     * @param deliveryAddress the delivery address for the order
     * @throws SQLException if a database access error occurs
     */
    public static boolean placeOrder(int customerId, List<CartProduct> cartProducts, String deliveryDate, String deliveryAddress, int deliveryWeight)
            throws SQLException {
        String orderQuery = "INSERT INTO orders (customer_id, delivery_date, delivery_address, delivery_weight) VALUES (?, ?, ?, ?)";
        String productsOrderedQuery = "INSERT INTO product_ordered (order_id, product_id, quantity) VALUES (?, ?, ?)";
        String updateProductQuery = "UPDATE products SET qty_available = qty_available - ? WHERE id = ?";

        try (Connection connection = connect()) {
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement orderStatement = connection.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement productsOrderedStatement = connection.prepareStatement(productsOrderedQuery);
                 PreparedStatement updateProductStatement = connection.prepareStatement(updateProductQuery)) {

                // Insert the order
                orderStatement.setInt(1, customerId);
                orderStatement.setString(2, deliveryDate);
                orderStatement.setString(3, deliveryAddress);
                orderStatement.setInt(4, deliveryWeight);
                orderStatement.executeUpdate();

                // Get the generated order_id
                ResultSet generatedKeys = orderStatement.getGeneratedKeys();
                if (!generatedKeys.next()) {
                    throw new SQLException("Failed to create order: No order ID obtained.");
                }
                int orderId = generatedKeys.getInt(1);

                // Insert each product into ProductsOrdered and update product quantity
                for (CartProduct cartProduct : cartProducts) {
                    int productId = cartProduct.getProduct().getId();
                    int quantity = cartProduct.getQuantity();

                    // Add to ProductsOrdered
                    productsOrderedStatement.setInt(1, orderId);
                    productsOrderedStatement.setInt(2, productId);
                    productsOrderedStatement.setInt(3, quantity);
                    productsOrderedStatement.executeUpdate();

                    // Update product quantity in products table
                    updateProductStatement.setInt(1, quantity);
                    updateProductStatement.setInt(2, productId);
                    updateProductStatement.executeUpdate();
                }

                connection.commit(); // Commit transaction
                return true;
            } catch (SQLException e) {
                connection.rollback(); // Rollback on error
                throw e;
            }
        }
    }
    
    public static List<Order> getCustomerOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT id, order_date, delivery_date, delivery_address, delivery_status FROM orders WHERE customer_id = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, SessionTracker.getSession().getId());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(new Order(
                    rs.getInt("id"),
                    rs.getDate("order_date").toLocalDate(),
                    rs.getDate("delivery_date").toLocalDate(),
                    rs.getString("delivery_address"),
                    rs.getString("delivery_status")
                ));
            }
        } catch (SQLException e) {
        	throw e;
        }

        return orders;
    }
    
   public static List<ProductOrdered> getProductsInOrder(int orderId) throws SQLException {
        List<ProductOrdered> products = new ArrayList<>();
        String query = """
            SELECT p.id, p.name, po.quantity, p.measuring_format, p.weight_per_unit 
            FROM product_ordered po
            JOIN products p ON po.product_id = p.id
            WHERE po.order_id = ?
        """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(new ProductOrdered(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("quantity"),
                    rs.getString("measuring_format"),
                    rs.getDouble("weight_per_unit")
                ));
            }
        } catch (SQLException e) {
        	throw e;
        }

        return products;
    }

}

