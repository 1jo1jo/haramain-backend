package haramain_backend.model;
import java.time.LocalDateTime;
import jakarta.persistence.Column; import jakarta.persistence.Entity; import jakarta.persistence.GeneratedValue; import jakarta.persistence.GenerationType; import jakarta.persistence.Id; import jakarta.persistence.Table;
@Entity @Table(name = "distribution") public class Distribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DistributionID")
    private int distributionID;

    @Column(name = "request_id")
    private int requestId;

    @Column(name = "OrganizationID")
    private int organizationID;

    @Column(name = "DistributionQuantity")
    private int distributionQuantity;

    @Column(name = "DistributionLocation")
    private String distributionLocation;

    @Column(name = "DistributionDate")
    private LocalDateTime distributionDate;

    @Column(name = "DistributionStatus")
    private String distributionStatus;

    public int getDistributionID() {
        return distributionID;
    }

    public void setDistributionID(int distributionID) {
        this.distributionID = distributionID;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getOrganizationID() {
        return organizationID;
    }

    public void setOrganizationID(int organizationID) {
        this.organizationID = organizationID;
    }

    public int getDistributionQuantity() {
        return distributionQuantity;
    }

    public void setDistributionQuantity(int distributionQuantity) {
        this.distributionQuantity = distributionQuantity;
    }

    public String getDistributionLocation() {
        return distributionLocation;
    }

    public void setDistributionLocation(String distributionLocation) {
        this.distributionLocation = distributionLocation;
    }

    public LocalDateTime getDistributionDate() {
        return distributionDate;
    }

    public void setDistributionDate(LocalDateTime distributionDate) {
        this.distributionDate = distributionDate;
    }

    public String getDistributionStatus() {
        return distributionStatus;
    }

    public void setDistributionStatus(String distributionStatus) {
        this.distributionStatus = distributionStatus;
    }
}