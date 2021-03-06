package ch.nexown.adresscrawler.model;

import ch.ech.xmlns.ech_0010._4.AddressInformationType;

/**
 * Created by buma on 26.01.2017.
 */
public class Address {

    private Long id;

    private String uId;

    private String companyName;

    private String owner;

    private AddressInformationType address;

    private String companyPurpose;

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AddressInformationType getAddress() {
        return address;
    }

    public void setAddress(AddressInformationType address) {
        this.address = address;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCompanyPurpose() {
        return companyPurpose;
    }

    public void setCompanyPurpose(String companyPurpose) {
        this.companyPurpose = companyPurpose;
    }
}
