package ch.nexown.adresscrawler.Service;

import ch.nexown.adresscrawler.Model.Address;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by buma on 26.01.2017.
 */

@RunWith(MockitoJUnitRunner.class)
public class AdressGrapperServiceTest {

    private AdressGrapperServiceImpl grapperService;

    @Before
    public void setUp() {
        grapperService = new AdressGrapperServiceImpl();
    }

    //105409360l SBB Bern


    @Test
    public void getInformation_from_webservice_test_request_zh() {
        Address address = grapperService.getInformation(151327050l);
        Address addressJson = jsonAddressReader("151327051");
        assertThat(address).extracting(
                Address::getCompanyName,
                Address::getId,
                Address::getCompanyPurpose,
                Address::getOwner)
                .contains(addressJson.getCompanyName(),
                        addressJson.getCompanyPurpose(),
                        addressJson.getOwner(),
                        addressJson.getId());
        assertThat(address.getCompanyName()).isEqualTo("ERNI Consulting AG");
    }

    @Test
    public void getInformation_from_webservice_test_request_ba() {
        Address address = grapperService.getInformation(101861442l);
        Address addressJson = jsonAddressReader("101861443");
        assertThat(address).extracting(
                Address::getCompanyName,
                Address::getId)
                .contains(addressJson.getCompanyName(),
                        addressJson.getId());
        assertThat(address.getCompanyName()).isEqualTo("Novartis Foundation");
    }

    @Test
    public void getInformation_from_webservice_test_request_zh_saxError() {
        Address address = grapperService.getInformation(101918969l);
        Address addressJson = jsonAddressReader("101918970");
        assertThat(address).extracting(
                Address::getCompanyName,
                Address::getId,
                Address::getCompanyPurpose,
                Address::getOwner)
                .contains(addressJson.getCompanyName(),
                        addressJson.getCompanyPurpose(),
                        addressJson.getOwner(),
                        addressJson.getId());
    }


    private Address jsonAddressReader(String id) {
        ObjectMapper mapper = new ObjectMapper();
        String path = new File("src\\main\\resources\\json\\" + id + ".json").getAbsolutePath();
        Address address = new Address();
        try {
            address = mapper.readValue(new File(path), Address.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

}

