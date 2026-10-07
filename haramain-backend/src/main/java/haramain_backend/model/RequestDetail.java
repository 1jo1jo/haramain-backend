package haramain_backend.model;
import jakarta.persistence.Column; import jakarta.persistence.Entity; import jakarta.persistence.GeneratedValue; import jakarta.persistence.GenerationType; import jakarta.persistence.Id; import jakarta.persistence.Table;
@Entity @Table(name = "request_detail") public class RequestDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RequestDateID")
    private int requestDateID;

    @Column(name = "request_id")
    private int requestId;

    @Column(name = "meal_id")
    private int mealId;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "UnitPrice")
    private double unitPrice;

    @Column(name = "TotalAmount")
    private double totalAmount;

    public int getRequestDateID() {
        return requestDateID;
    }

    public void setRequestDateID(int requestDateID) {
        this.requestDateID = requestDateID;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getMealId() {
        return mealId;
    }

    public void setMealId(int mealId) {
        this.mealId = mealId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}