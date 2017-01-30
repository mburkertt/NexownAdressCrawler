package ch.nexown.adresscrawler.Service;


import ch.admin.e_service.zefix._2015_06_26.*;
import ch.ech.xmlns.ech_0010._4.AddressInformationType;
import ch.nexown.adresscrawler.Model.Address;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
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
        LOG.info("End Process");
    }


    Address getInformation(long id) {
        long nextChId = id + 1;
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

    private long getLastId() {
        ObjectMapper mapper = new ObjectMapper();
        String pathLast = new File("src\\main\\resources\\json\\last.json").getAbsolutePath();
        Address address = new Address();
        try {
            address = mapper.readValue(new File(pathLast), Address.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address.getId();
    }

    private void writeJson(Address address) {
        ObjectMapper mapper = new ObjectMapper();
        //Object to JSON in file
        try {
            if (address.getCompanyName() != "No Company with this CHID") {
                String path = new File("src\\main\\resources\\json\\" + address.getId() + ".json").getAbsolutePath();
                mapper.writeValue(new File(path), address);
                LOG.info("File {} was Written in Folder ", address);
            }
            String pathLast = new File("src\\main\\resources\\json\\last.json").getAbsolutePath();
            mapper.writeValue(new File(pathLast), address);
        } catch (IOException e) {
            LOG.error("Error during json generation {}", e);
        }
    }

    private Address getAddressesOutOfCompanyInfo(List<CompanyDetailedInfoType> result, long id) {
        Address address = new Address();
        for (CompanyDetailedInfoType companyDetailedInfoType : result) {
            address = getAdressNullSafe(companyDetailedInfoType);
            address.setId(id);
        }
        return address;
    }

    private Address getAdressNullSafe(CompanyDetailedInfoType companyDetailedInfoType) {
        Address address = new Address();
        if (companyDetailedInfoType.getAddress() == null
                || companyDetailedInfoType.getAddress().getAddressInformation() == null) {
            address.setAddress(new AddressInformationType());
        } else {
            address.setAddress(companyDetailedInfoType.getAddress().getAddressInformation());
        }
        if (companyDetailedInfoType.getName() == null
                || StringUtils.isEmpty(companyDetailedInfoType.getName())) {
            address.setCompanyName("No Company name Available");
        } else {
            address.setCompanyName(companyDetailedInfoType.getName());
        }
        if (companyDetailedInfoType.getPurpose() == null
                || StringUtils.isEmpty(companyDetailedInfoType.getPurpose())) {
            address.setCompanyPurpose("No Companypurpose Available");
        } else {
            address.setCompanyPurpose(companyDetailedInfoType.getPurpose());
        }
        if (companyDetailedInfoType.getAddress() == null
                || companyDetailedInfoType.getAddress().getPerson() == null) {
            address.setOwner("No Owner Registered");
        } else {
            address.setOwner(companyDetailedInfoType.getAddress().getPerson().toString());
        }
        writeJson(address);
        return address;
    }

    private String getChid(long id) {
        String missing = getMissing(getMissingDecimalPoints(id), id);
        return "CH" + missing + id;
    }

    private long getMissingDecimalPoints(long number) {
        double currentDecimalPoints = Math.floor(Math.log10(number)) + 1;
        double completeDecimalNumer = CH_DECIMAL_POINTS;
        double missingDecimalPoints = completeDecimalNumer - currentDecimalPoints;
        return (long) missingDecimalPoints;
    }

    private String getMissing(long missingDecimalPoints, long id) {
        StringBuilder stringBuilder = new StringBuilder();
        for (long i = missingDecimalPoints; i > 0; i--) {
            stringBuilder.append("0");
        }
        return stringBuilder.toString();
    }

}
