package ch.nexown.adresscrawler.service;

import ch.nexown.adresscrawler.model.Address;

import java.util.List;

public interface FileWriterService {


    void writeToJson(List<Address> addresses, String fileName);

    void writeToCSV(List<Address> addressArrayList, String fileName);

    List<Address> readAddressesFromCSV(String fileName);

    List<Address> readAddressesFromJson(String fileName);
}
