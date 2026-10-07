package haramain_backend.model;
import jakarta.persistence.Column; import jakarta.persistence.Entity; import jakarta.persistence.Id; import jakarta.persistence.Table;
@Entity
@Table(name = "organisation")
public class Organisation {
@Id
@Column(name = "organisationID")
private int organisationID;

@Column(name = "organisationName")
private String organisationName;

@Column(name = "phone")
private String phone;

@Column(name = "address")
private String address;

public int getOrganisationID() {
    return organisationID;
}

public void setOrganisationID(int organisationID) {
    this.organisationID = organisationID;
}

public String getOrganisationName() {
    return organisationName;
}

public void setOrganisationName(String organisationName) {
    this.organisationName = organisationName;
}

public String getPhone() {
    return phone;
}

public void setPhone(String phone) {
    this.phone = phone;
}

public String getAddress() {
    return address;
}

public void setAddress(String address) {
    this.address = address;
}
}