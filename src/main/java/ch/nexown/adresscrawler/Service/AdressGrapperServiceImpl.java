package ch.nexown.adresscrawler.Service;


import ch.admin.e_service.zefix._2015_06_26.*;
import ch.ech.xmlns.ech_0010._4.AddressInformationType;
import ch.nexown.adresscrawler.Model.Address;
import ch.nexown.adresscrawler.xml.workflow.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.BindingProvider;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;


/**
 * Created by buma on 26.01.2017.
 */
@Service
public class AdressGrapperServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(AdressGrapperServiceImpl.class);
    private static final int TIMING = 6;
    private static final int CH_DECIMAL_POINTS = 11;
    private ZefixService zefixService = new ZefixService();

    private static String getUrlSource(String url) throws IOException {
        URL yahoo = new URL(url);
        URLConnection yc = yahoo.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                yc.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder a = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            a.append(inputLine);
        }
        in.close();

        return a.toString();
    }

    @Scheduled(fixedRate = TIMING)
    public void webServiceCall() {
        LOG.info("Start crawling");
        getInformation(getLastId());
        LOG.info("End Process");
    }

    Address getInformation(Long id) {
        long nextChId = id + 1;
        ZefixServicePortType port = zefixService.getZefixServicePort();

        BindingProvider prov = (BindingProvider) port;
        prov.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "selim.akyol@nexown.ch");
        prov.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "z8rgvtFh");
        GetByUidRequestType getByUidRequestType = new GetByUidRequestType();
        getByUidRequestType.setUid(Math.toIntExact(id));
        DetailledResponseType result = port.getByUidDetailled(getByUidRequestType);
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

    private Long getLastId() {
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

    private Address getAddressesOutOfCompanyInfo(List<CompanyDetailedInfoType> result, Long id) {
        Address address = new Address();
        for (CompanyDetailedInfoType companyDetailedInfoType : result) {
            address = getAdressNullSafe(companyDetailedInfoType, id);
        }
        return address;
    }

    private Address getAdressNullSafe(CompanyDetailedInfoType companyDetailedInfoType, Long id) {
        Address address = new Address();
        address.setId(id);
        if (companyDetailedInfoType.getChid() == null) {
            address.setuId("no CHId availbable");
        } else {
            address.setuId(companyDetailedInfoType.getChid());
        }
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
        if (getPurpose(companyDetailedInfoType) == null
                || StringUtils.isEmpty(getPurpose(companyDetailedInfoType))) {
            address.setCompanyPurpose("No Companypurpose Available");
        } else {
            address.setCompanyPurpose(companyDetailedInfoType.getPurpose());
        }
        if (getOwner(companyDetailedInfoType) == null
                || StringUtils.isEmpty(getOwner(companyDetailedInfoType))) {
            address.setOwner("No Owner Registered");
        } else {
            address.setOwner(getOwner(companyDetailedInfoType));
        }
        writeJson(address);
        return address;
    }

    private String getOwner(CompanyDetailedInfoType companyDetailedInfoType) {
        Excerpt objectFactory = getWsLinkInformation(companyDetailedInfoType);
        if (excerptIsNullSafe(objectFactory)) {
            Persons persons = objectFactory.getInstances().getInstance().getRubrics().getPersons();
            List<Person> personList = persons.getPerson();
            StringBuilder stringBuilder = new StringBuilder();
            for (Person person : personList) {
                Particulars particulars = person.getParticulars();
                stringBuilder.append(particulars.getPersonText());
                stringBuilder.append("\n");
            }
            return stringBuilder.toString();
        } else {
            return "No Owner Found";
        }
    }

    private boolean excerptIsNullSafe(Excerpt excerpt) {
        if (excerpt != null
                && excerpt.getInstances() != null
                && excerpt.getInstances().getInstance() != null
                && excerpt.getInstances().getInstance().getRubrics() != null
                && excerpt.getInstances().getInstance().getRubrics().getPersons() != null
                && excerpt.getInstances().getInstance().getRubrics().getPersons().getPerson().size() > 0) {
            return true;
        }
        return false;
    }

    private boolean excerptIsNullSafePurpose(Excerpt excerpt) {
        if (excerpt != null
                && excerpt.getInstances() != null
                && excerpt.getInstances().getInstance() != null
                && excerpt.getInstances().getInstance().getRubrics() != null
                && excerpt.getInstances().getInstance().getRubrics().getPurposes() != null
                && excerpt.getInstances().getInstance().getRubrics().getPurposes().getPurpose() != null) {
            return true;
        }
        return false;
    }

    private String getPurpose(CompanyDetailedInfoType companyDetailedInfoType) {
        Excerpt objectFactory = getWsLinkInformation(companyDetailedInfoType);
        if (excerptIsNullSafePurpose(objectFactory)) {
            Purposes purposes = objectFactory.getInstances().getInstance().getRubrics().getPurposes();
            if (purposes.getPurpose() == null
                    || purposes.getPurpose().getValue() == null) {
                return "No Purpose Available";
            } else {
                Purpose purpose = purposes.getPurpose();
                return purpose.getValue();
            }
        } else {
            return "no purpose Found";
        }

    }

    private Excerpt getWsLinkInformation(CompanyDetailedInfoType companyDetailedInfoType) {
        String wsLink = companyDetailedInfoType.getWsLink();
        String xmlString = "";
        try {
            xmlString = getUrlSource(wsLink);
        } catch (IOException e) {
            LOG.error("Error while getting Web Sources: {}", e);
        }
        JAXBContext jaxbContext = getJaxbContext();
        if (jaxbContext != null) {
            Excerpt objectFactory = unmarshallObjectFactory(jaxbContext, xmlString);
            return objectFactory;
        } else {
            LOG.error("jaxbContext was null no Instanciation possible");
            return null;
        }
    }

    private Excerpt unmarshallObjectFactory(JAXBContext jaxbContext, String xmlString) {
        Excerpt objectFactory = null;
        Unmarshaller jaxbUnmarshaller = null;
        try {
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            LOG.error("Failed to Instanciate jaxbUnmarshaller: {}", e);
        }
        StringReader reader = new StringReader(xmlString);
        try {
            objectFactory = (Excerpt) jaxbUnmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            LOG.error("Binding failed: {}", e);
        }
        return objectFactory;
    }


    private JAXBContext getJaxbContext() {
        JAXBContext jaxbContext = null;
        try {
            jaxbContext = JAXBContext.newInstance(Excerpt.class);
        } catch (JAXBException e) {
            LOG.error("Error creating Jaxbcontext: {}", e);
        }
        return jaxbContext;
    }

}
