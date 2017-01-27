package ch.nexown.adresscrawler.Service;


import ch.admin.e_service.zefix._2015_06_26.*;
import ch.nexown.adresscrawler.Model.Address;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.xml.ws.BindingProvider;
import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * Created by buma on 26.01.2017.
 */
@Service
public class AdressGrapperServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(AdressGrapperServiceImpl.class);
    private static final int TIMING = 600;
    private static final int CH_DECIMAL_POINTS = 11;
    private ZefixService zefixService = new ZefixService();

    @Scheduled(fixedRate = TIMING)
    public void webServiceCall() {
        LOG.info("Start crawling");
        getInformation(getLastId());
        LOG.info("Put new json in Folder");
    }


    Address getInformation(int id) {
        int nextChId = id + 1;
        ZefixServicePortType port = zefixService.getZefixServicePort();

        BindingProvider prov = (BindingProvider) port;
        prov.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "selim.akyol@nexown.ch");
        prov.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "z8rgvtFh");
        GetByCHidRequestType getByCHidRequestType = new GetByCHidRequestType();
        String chId = getChid(nextChId);
        getByCHidRequestType.setChid(chId);
        DetailledResponseType result = port.getByCHidDetailled(getByCHidRequestType);
        Address address;
        if (result.getErrors() != null || result.getResult().getCompanyInfo().size() == 0) {
            Address defaultAddress = new Address();
            defaultAddress.setId(nextChId);
            defaultAddress.setCompanyName("No Company with this CHID");
            writeJson(defaultAddress);
            return defaultAddress;
        } else {
            address = getAddressesOutOfCompanyInfo(result.getResult().getCompanyInfo(), nextChId);
            return address;
        }
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

    private Address getAddressesOutOfCompanyInfo(List<CompanyDetailedInfoType> result, int id) {
        Address address = new Address();
        for (CompanyDetailedInfoType companyDetailedInfoType : result) {
            address.setAddress(companyDetailedInfoType.getAddress().getAddressInformation());
            address.setCompanyName(companyDetailedInfoType.getName());
            address.setCompanyPurpose(companyDetailedInfoType.getPurpose());
            address.setId(id);
            if (companyDetailedInfoType.getAddress().getPerson() == null) {
                address.setOwner("No Owner Registered");
            } else {
                address.setOwner(companyDetailedInfoType.getAddress().getPerson().toString());
            }
            writeJson(address);
        }
        return address;
    }

    private String getChid(int id) {
        String missing = getMissing(getMissingDecimalPoints(id), id);
        return "CH" + missing + id;
    }

    private int getMissingDecimalPoints(int number) {
        double currentDecimalPoints = Math.floor(Math.log10(number)) + 1;
        double completeDecimalNumer = CH_DECIMAL_POINTS;
        double missingDecimalPoints = completeDecimalNumer - currentDecimalPoints;
        return (int) missingDecimalPoints;
    }

    private String getMissing(int missingDecimalPoints, int id) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = missingDecimalPoints; i > 0; i--) {
            stringBuilder.append("0");
        }
        return stringBuilder.toString();
    }

}
