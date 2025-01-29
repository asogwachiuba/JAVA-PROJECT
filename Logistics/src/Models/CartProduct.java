package Models;

//CartProduct class for handling the quantity counter in the cart
public class CartProduct {
	private Product product;
	private int quantity;

	public CartProduct(Product product, int quantity) {
		this.product = product;
		this.quantity = quantity;
	}

	public Product getProduct() {
		return product;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	@Override
	public String toString() {
		return product.getName() + " (" + quantity + " units) - " + product.getWeightPerUnit() + " kg each";
	}
}
