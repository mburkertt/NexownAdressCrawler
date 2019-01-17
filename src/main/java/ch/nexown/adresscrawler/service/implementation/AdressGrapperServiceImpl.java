package ch.nexown.adresscrawler.service.implementation;

import ch.admin.e_service.zefix._2015_06_26.*;
import ch.ech.xmlns.ech_0010._4.AddressInformationType;
import ch.nexown.adresscrawler.model.Address;
import ch.nexown.adresscrawler.model.LegalSeat;
import ch.nexown.adresscrawler.service.AdressGrapperService;
import ch.nexown.adresscrawler.service.FileWriterService;
import ch.nexown.adresscrawler.xml.workflow.ba.Extract;
import ch.nexown.adresscrawler.xml.workflow.ba.ExtractList;
import ch.nexown.adresscrawler.xml.workflow.ba.Zweck;
import ch.nexown.adresscrawler.xml.workflow.zh.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by buma on 26.01.2017.
 */
@Service
public class AdressGrapperServiceImpl implements AdressGrapperService {

    private ZefixService zefixService = new ZefixService();
    private FileWriterService writerService;

    private static final Logger LOG = LoggerFactory.getLogger(AdressGrapperServiceImpl.class);
    private static final int TIMING = 1;
    private static int csvwriteammount = 300;
    private static final String ADDRESSESNAME = "Adresses";
    private List<Address> adresses = new ArrayList<>();
    private long nextChId;
    private ZefixServicePortType port = zefixService.getZefixServicePort();

    public AdressGrapperServiceImpl(FileWriterService writerService) {
        this.writerService = writerService;
        BindingProvider prov = (BindingProvider) port;
        prov.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "selim.akyol@nexown.ch");
        prov.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "z8rgvtFh");
        adresses = writerService.readAddressesFromCSV(ADDRESSESNAME);
    }

    @Scheduled(fixedRate = TIMING)
    public void webServiceCall() {
        Address adress = getInformation(getLastId());
        if (adress != null) {
            adresses.add(adress);
            LOG.info("Address added to list size is now: {}", adresses.size());
        }
        if (adresses.size() > csvwriteammount) {
            writerService.writeToCSV(adresses, ADDRESSESNAME);
            writerService.writeToJson(adresses, ADDRESSESNAME);
            LOG.info("Addresses {} printed in csv", adresses.size());
            csvwriteammount = csvwriteammount + 20;
        }
    }

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

    Address getInformation(Long id) {
        nextChId = id + 1;
        GetByUidRequestType getByUidRequestType = new GetByUidRequestType();
        getByUidRequestType.setUid(Math.toIntExact(id));
        DetailledResponseType result = port.getByUidDetailled(getByUidRequestType);
        Address address;
        if (result.getErrors() != null || result.getResult().getCompanyInfo().size() == 0) {
            return null;
        } else {
            address = getAddressesOutOfCompanyInfo(result.getResult().getCompanyInfo(), nextChId);
            return address;
        }
    }

    private Long getLastId() {
        ObjectMapper mapper = new ObjectMapper();
        if (nextChId == 0) {
            String pathLast = new File("src\\main\\resources\\json\\last.json").getAbsolutePath();
            Address address = new Address();
            try {
                address = mapper.readValue(new File(pathLast), Address.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return address.getId();
        }
        return nextChId;
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
        getOwnerAndPurposeDependingOnKanton(address, companyDetailedInfoType);
        writerService.writeToJson(Collections.singletonList(address), ADDRESSESNAME);
        return address;
    }

    private Address getOwnerAndPurposeDependingOnKanton(Address address, CompanyDetailedInfoType companyDetailedInfoType) {
        if (companyDetailedInfoType.getLegalSeat().contains(LegalSeat.ZH)) {
            return getAdressZh(address, companyDetailedInfoType);
        }
        if (companyDetailedInfoType.getLegalSeat().contains(LegalSeat.BA)) {
            return getAdressBa(address, companyDetailedInfoType);
        }
        address.setCompanyPurpose("Kanton service not implemented yet");
        address.setOwner("Kanton service not implemented yet");
        return address;
    }

    private Address getAdressBa(Address address, CompanyDetailedInfoType companyDetailedInfoType) {
        if (getPurposeBa(companyDetailedInfoType) == null
                || StringUtils.isEmpty(getPurposeBa(companyDetailedInfoType))) {
            address.setCompanyPurpose("No Companypurpose Available");
        } else {
            address.setCompanyPurpose(getPurposeBa(companyDetailedInfoType));
        }
        if (getOwnerBa(companyDetailedInfoType) == null
                || StringUtils.isEmpty(getOwnerBa(companyDetailedInfoType))) {
            address.setOwner("No Owner Registered");
        } else {
            address.setOwner(getOwnerBa(companyDetailedInfoType));
        }
        return address;
    }

    private String getPurposeBa(CompanyDetailedInfoType companyDetailedInfoType) {
        ExtractList objectFactory = getWsLinkInformationBa(companyDetailedInfoType);
        if (extractListIsNullSafe(objectFactory)) {
            List<Zweck> zweckList = objectFactory.getExtract().getZwecke().getZweck();
            StringBuilder stringBuilder = new StringBuilder();
            for (Zweck zweck : zweckList) {
                stringBuilder.append(zweck.getValue());
                stringBuilder.append("\n");
            }
            return stringBuilder.toString();
        } else {
            return "No Owner Found";
        }
    }

    private String getOwnerBa(CompanyDetailedInfoType companyDetailedInfoType) {
        ExtractList objectFactory = getWsLinkInformationBa(companyDetailedInfoType);
        if (extractListIsNullSafe(objectFactory)) {
            List<ch.nexown.adresscrawler.xml.workflow.ba.Person> personList = objectFactory.getExtract().getPersonal().getPerson();
            StringBuilder stringBuilder = new StringBuilder();
            for (ch.nexown.adresscrawler.xml.workflow.ba.Person person : personList) {
                stringBuilder.append(person.getPersonalien().getPersDaten());
                stringBuilder.append("\n");
            }
            return stringBuilder.toString();
        } else {
            return "No Owner Found";
        }
    }

    private boolean extractListIsNullSafe(ExtractList objectFactory) {
        if (objectFactory == null || objectFactory.getExtract() == null) {
            return false;
        }
        return true;
    }

    private Address getAdressZh(Address address, CompanyDetailedInfoType companyDetailedInfoType) {
        if (getPurposeZh(companyDetailedInfoType) == null
                || StringUtils.isEmpty(getPurposeZh(companyDetailedInfoType))) {
            address.setCompanyPurpose("No Companypurpose Available");
        } else {
            address.setCompanyPurpose(companyDetailedInfoType.getPurpose());
        }
        if (getOwnerZh(companyDetailedInfoType) == null
                || StringUtils.isEmpty(getOwnerZh(companyDetailedInfoType))) {
            address.setOwner("No Owner Registered");
        } else {
            address.setOwner(getOwnerZh(companyDetailedInfoType));
        }
        return address;
    }


    private String getOwnerZh(CompanyDetailedInfoType companyDetailedInfoType) {
        Excerpt objectFactory = getWsLinkInformationZh(companyDetailedInfoType);
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

    private String getPurposeZh(CompanyDetailedInfoType companyDetailedInfoType) {
        Excerpt objectFactory = getWsLinkInformationZh(companyDetailedInfoType);
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

    private Excerpt getWsLinkInformationZh(CompanyDetailedInfoType companyDetailedInfoType) {
        JAXBContext jaxbContext = getJaxbContextZh();
        String wsLink = companyDetailedInfoType.getWsLink();
        String xmlString = "";
        try {
            xmlString = getUrlSource(wsLink);
        } catch (IOException e) {
            LOG.error("Error while getting Web Sources: {}", e);
        }
        if (jaxbContext != null) {
            Excerpt objectFactory = unmarshallObjectFactoryZh(jaxbContext, xmlString);
            return objectFactory;
        } else {
            LOG.error("jaxbContext was null no Instanciation possible");
            return null;
        }
    }

    private ExtractList getWsLinkInformationBa(CompanyDetailedInfoType companyDetailedInfoType) {
        JAXBContext jaxbContext = getJaxbContextBa();
        String wsLink = companyDetailedInfoType.getWsLink();
        String xmlString = "";
        try {
            xmlString = getUrlSource(wsLink);
        } catch (IOException e) {
            LOG.error("Error while getting Web Sources: {}", e);
        }
        if (jaxbContext != null) {
            ExtractList objectFactory = unmarshallObjectFactoryBa(jaxbContext, xmlString);
            return objectFactory;
        } else {
            LOG.error("jaxbContext was null no Instanciation possible");
            return null;
        }
    }

    private ExtractList unmarshallObjectFactoryBa(JAXBContext jaxbContext, String xmlString) {
        ExtractList objectFactory = null;
        Unmarshaller jaxbUnmarshaller = null;
        try {
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            LOG.error("Failed to Instanciate jaxbUnmarshaller: {}", e);
        }
        try {
            StringReader reader = new StringReader(xmlString);
            objectFactory = (ExtractList) jaxbUnmarshaller.unmarshal(reader);
            Extract extract = new Extract();
        } catch (JAXBException e1) {
            LOG.error("Binding failed: {}", e1);
        }
        return objectFactory;
    }


    private Excerpt unmarshallObjectFactoryZh(JAXBContext jaxbContext, String xmlString) {
        Excerpt objectFactory = null;
        Unmarshaller jaxbUnmarshaller = null;
        try {
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            LOG.error("Failed to Instanciate jaxbUnmarshaller: {}", e);
        }
        StringReader reader = new StringReader(xmlString);
        Source xmlInput = new StreamSource(new StringReader(xmlString));
        try {
            objectFactory = (Excerpt) jaxbUnmarshaller.unmarshal(xmlInput);
        } catch (JAXBException e1) {
            LOG.error("Binding failed: {}", e1);
        }
        return objectFactory;
    }


    private JAXBContext getJaxbContextZh() {
        JAXBContext jaxbContext = null;
        try {
            jaxbContext = JAXBContext.newInstance(Excerpt.class);
        } catch (JAXBException e) {
            LOG.error("Error creating Jaxbcontext: {}", e);
        }
        return jaxbContext;
    }

    private JAXBContext getJaxbContextBa() {
        JAXBContext jaxbContext = null;
        try {
            jaxbContext = JAXBContext.newInstance(ExtractList.class);
        } catch (JAXBException e) {
            LOG.error("Error creating Jaxbcontext: {}", e);
        }
        return jaxbContext;
    }

}
