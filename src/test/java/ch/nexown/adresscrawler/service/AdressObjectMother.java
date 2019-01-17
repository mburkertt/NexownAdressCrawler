package ch.nexown.adresscrawler.service;

import ch.ech.xmlns.ech_0010._4.AddressInformationType;
import ch.nexown.adresscrawler.model.Address;

public class AdressObjectMother {

    public static Address GetErniAdress(){
        Address erni = new Address();
        erni.setId(151327051l);
        erni.setuId("CH02090030064");
        erni.setOwner("No Owner Found");
        erni.setCompanyName("ERNI Schweiz AG");
        AddressInformationType erniAdress = new AddressInformationType();
        erniAdress.setStreet("Thurgauerstrasse");
        erniAdress.setHouseNumber("40");
        erniAdress.setTown("Zürich");
        erniAdress.setSwissZipCode(8050l);
        erniAdress.setCountry("CH");
        erni.setAddress(erniAdress);
        erni.setCompanyPurpose("ERNI Schweiz AG");
        return  erni;
    };
}
