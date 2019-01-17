package ch.nexown.adresscrawler.service.implementation;

import ch.ech.xmlns.ech_0010._4.AddressInformationType;
import ch.nexown.adresscrawler.model.Address;
import ch.nexown.adresscrawler.service.FileWriterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileWriterServiceImpl implements FileWriterService {

    private static final char CSV_SEPARATOR = ';';
    private static final Logger LOG = LoggerFactory.getLogger(FileWriterServiceImpl.class);

    @Override
    public void writeToJson(List<Address> addresses, String fileName) {
        ObjectMapper mapper = new ObjectMapper();
        String path = new File("src\\main\\resources\\json\\" + fileName + ".json").getAbsolutePath();
        try {
            mapper.writeValue(new File(path), addresses);
        } catch (IOException ioException) {
            LOG.error("Error while writing adress list in Json {}", ioException);
        }
        LOG.info("File {} was Written in Folder ", addresses);
    }

    @Override
    public void writeToCSV(List<Address> addressArrayList, String fileName) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName + ".csv"), "UTF-8"));
            int iterator = 0;
            for (Address address : addressArrayList) {
                StringBuffer oneLine = new StringBuffer();
                if (iterator == 0) {
                    StringBuffer headLine = new StringBuffer();
                    writeInformationLineInCsv(headLine);
                    bufferedWriter.write(headLine.toString());
                    bufferedWriter.newLine();
                }
                oneLine.append(iterator);
                writeAdressIntoCsvLine(oneLine, address);
                bufferedWriter.write(oneLine.toString());
                bufferedWriter.newLine();
                iterator++;
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error during writing CSV {}", e);
        } catch (FileNotFoundException e) {
            LOG.error("Error during writing CSV {}", e);
        } catch (IOException e) {
            LOG.error("Error during writing CSV {}", e);
        }
    }

    @Override
    public List<Address> readAddressesFromCSV(String fileName) {
        List<Address> addresses = new ArrayList<>();
        CSVReader reader = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName.concat(".csv"));
            reader = new CSVReader(new InputStreamReader(fileInputStream, "UTF-8"));
            String[] csvFileAsArray;
            while ((csvFileAsArray = reader.readNext()) != null) {
                Address address = readAdressFromCsvLine(csvFileAsArray);
                if (address != null) {
                    addresses.add(address);
                }
            }
        } catch (IOException e) {
            LOG.error("Error during reading CSV {}", e);
        }
        return addresses;
    }

    @Override
    public List<Address> readAddressesFromJson(String fileName) {
        ObjectMapper mapper = new ObjectMapper();
        String path = new File("src\\main\\resources\\json\\" + fileName + ".json").getAbsolutePath();
        List<Address> addresses = new ArrayList<>();
        try {
            addresses = mapper.readValue(new File(path), new TypeReference<List<Address>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    private Address readAdressFromCsvLine(String[] line) {
        Address address = new Address();
        String[] fields = line[0].split(";");
        try {
            address.setId(Long.valueOf(fields[1]));
            address.setuId(fields[2]);
            address.setCompanyName(fields[3]);
            address.setOwner(fields[4]);
            address.setCompanyPurpose(fields[5]);
            AddressInformationType addressInformationType = new AddressInformationType();
            addressInformationType.setAddressLine1(fields[6]);
            addressInformationType.setAddressLine2(fields[7]);
            addressInformationType.setTown(fields[8]);
            addressInformationType.setCountry(fields[9]);
            addressInformationType.setForeignZipCode(fields[10]);
            addressInformationType.setHouseNumber(fields[11]);
            addressInformationType.setStreet(fields[12]);
            addressInformationType.setDwellingNumber(fields[13]);
            addressInformationType.setLocality(fields[14]);
            addressInformationType.setSwissZipCodeAddOn(fields[15]);
            addressInformationType.setSwissZipCode(Long.valueOf(fields[16]));
            address.setAddress(addressInformationType);
        } catch (Exception e) {
            LOG.error("First Line Read Error");
            return null;
        }
        return address;
    }


    private void writeAdressIntoCsvLine(StringBuffer oneLine, Address address) {
        oneLine.append(CSV_SEPARATOR);
        oneLine.append(address.getId());
        oneLine.append(CSV_SEPARATOR);
        oneLine.append(address.getuId());
        oneLine.append(CSV_SEPARATOR);
        oneLine.append(address.getCompanyName());
        oneLine.append(CSV_SEPARATOR);
        oneLine.append(address.getOwner());
        oneLine.append(CSV_SEPARATOR);
        oneLine.append(address.getCompanyPurpose());
        oneLine.append(CSV_SEPARATOR);
        oneLine.append(address.getAddress().getAddressLine1());
        oneLine.append(CSV_SEPARATOR);
        oneLine.append(address.getAddress().getAddressLine2());
        oneLine.append(CSV_SEPARATOR);
        oneLine.append(address.getAddress().getTown());
        oneLine.append(CSV_SEPARATOR);
        oneLine.append(address.getAddress().getCountry());
        oneLine.append(CSV_SEPARATOR);
        oneLine.append(address.getAddress().getForeignZipCode());
        oneLine.append(CSV_SEPARATOR);
        oneLine.append(address.getAddress().getHouseNumber());
        oneLine.append(CSV_SEPARATOR);
        oneLine.append(address.getAddress().getStreet());
        oneLine.append(CSV_SEPARATOR);
        oneLine.append(address.getAddress().getDwellingNumber());
        oneLine.append(CSV_SEPARATOR);
        oneLine.append(address.getAddress().getLocality());
        oneLine.append(CSV_SEPARATOR);
        oneLine.append(address.getAddress().getSwissZipCodeAddOn());
        oneLine.append(CSV_SEPARATOR);
        oneLine.append(address.getAddress().getSwissZipCode());
        oneLine.append(CSV_SEPARATOR);
    }


    private void writeInformationLineInCsv(StringBuffer oneLine) {
        oneLine.append("Number");
        oneLine.append(CSV_SEPARATOR);
        oneLine.append("Id");
        oneLine.append(CSV_SEPARATOR);
        oneLine.append("Unique Id");
        oneLine.append(CSV_SEPARATOR);
        oneLine.append("Company Name");
        oneLine.append(CSV_SEPARATOR);
        oneLine.append("Company owner");
        oneLine.append(CSV_SEPARATOR);
        oneLine.append("Company purpose");
        oneLine.append(CSV_SEPARATOR);
        oneLine.append("First adressline");
        oneLine.append(CSV_SEPARATOR);
        oneLine.append("second adressline");
        oneLine.append(CSV_SEPARATOR);
        oneLine.append("Country");
        oneLine.append(CSV_SEPARATOR);
        oneLine.append("Foreign Zip Code");
        oneLine.append(CSV_SEPARATOR);
        oneLine.append("House number");
        oneLine.append(CSV_SEPARATOR);
        oneLine.append("Street");
        oneLine.append(CSV_SEPARATOR);
        oneLine.append("Dwelling number");
        oneLine.append(CSV_SEPARATOR);
        oneLine.append("Locality");
        oneLine.append(CSV_SEPARATOR);
        oneLine.append("Swiss zipcode add on");
        oneLine.append(CSV_SEPARATOR);
        oneLine.append("Swiss zipcode");
        oneLine.append(CSV_SEPARATOR);
    }
}
