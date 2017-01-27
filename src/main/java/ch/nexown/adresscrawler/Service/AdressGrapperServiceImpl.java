package ch.nexown.adresscrawler.Service;

import ch.admin.e_service.zefix._2015_06_26.*;
import ch.nexown.adresscrawler.Model.Address;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.xml.ws.BindingProvider;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by buma on 26.01.2017.
 */
@Service
public class AdressGrapperServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(AdressGrapperServiceImpl.class);

    private ZefixService zefixService = new ZefixService();

    public void webServiceCall() {
        getInformation(getLastId());
    }


    public List<Address> getInformation(int id) {
        ZefixServicePortType port = zefixService.getZefixServicePort();

        BindingProvider prov = (BindingProvider) port;
        prov.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "selim.akyol@nexown.ch");
        prov.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "z8rgvtFh");


        GetByCHidRequestType getByCHidRequestType = new GetByCHidRequestType();
        String chId = getChid(id);
        getByCHidRequestType.setChid(chId);
        DetailledResponseType result = port.getByCHidDetailled(getByCHidRequestType);
        List<Address> addresses = getAddressesOutOfCompanyInfo(result.getResult().getCompanyInfo());
        return addresses;
    }

    private int getLastId() {
        ObjectMapper mapper = new ObjectMapper();
        String pathLast = new File("src\\main\\resources\\json\\last.json").getAbsolutePath();
        Address address = new Address();
        try {
            address = mapper.readValue(new File(pathLast), Address.class);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return address.getId();
    }

    private void writeJson(Address address) {
        ObjectMapper mapper = new ObjectMapper();
        //Object to JSON in file
        try {
            String path = new File("src\\main\\resources\\json\\" + address.getId() + ".json").getAbsolutePath();
            String pathLast = new File("src\\main\\resources\\json\\last.json").getAbsolutePath();
            mapper.writeValue(new File(pathLast), address);
            mapper.writeValue(new File(path), address);
        } catch (IOException e) {
            LOG.error("Error during json generation {}", e);
        }
    }

    private List<Address> getAddressesOutOfCompanyInfo(List<CompanyDetailedInfoType> result) {
        List<Address> addresses = new ArrayList<>();
        for (CompanyDetailedInfoType companyDetailedInfoType : result) {
            Address address = new Address();
            address.setAddress(companyDetailedInfoType.getAddress().getAddressInformation());
            address.setCompanyName(companyDetailedInfoType.getName());
            address.setCompanyPurpose(companyDetailedInfoType.getPurpose());
            address.setOwner(companyDetailedInfoType.getAddress().getPerson().getFirstName() + companyDetailedInfoType.getAddress().getPerson().getLastName());
            addresses.add(address);
            writeJson(address);
        }
        return addresses;
    }

    private String getChid(int id) {
        String missing = getMissing(getMissingDecimalPoints(id));
        return "CH" + missing + id;
    }

    private int getMissingDecimalPoints(int number) {
        double missingDecimalPoints = Math.floor(Math.log10(number)) + 1;
        return (int) missingDecimalPoints;
    }

    private String getMissing(int missingDecimalPoints) {
        String missing = "";
        for (int i = missingDecimalPoints; i > 0; i--) {
            missing.concat("0");
        }
        return missing;
    }

}
