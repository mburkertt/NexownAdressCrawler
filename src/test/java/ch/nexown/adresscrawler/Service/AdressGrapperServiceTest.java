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

    @Test
    public void getInformation_from_webservice_test_request() {
        Address address = grapperService.getInformation(2090030063);
        Address addressJson = jsonAddressReader("2090030064");
        assertThat(address).extracting(
                Address::getCompanyName,
                Address::getCompanyPurpose,
                Address::getId,
                Address::getOwner)
                .contains(addressJson.getCompanyName(),
                        addressJson.getCompanyPurpose(),
                        addressJson.getId(),
                        addressJson.getOwner());
        assertThat(address.getCompanyName()).isEqualTo("ERNI Consulting AG");
    }


    @Test
    public void getInformation_from_webservice_test_request_with_id_one() {
        Address address = grapperService.getInformation(2090034467l);
        Address addressJson = jsonAddressReader("last");
        assertThat(address).extracting(Address::getAddress,
                Address::getCompanyName,
                Address::getId)
                .contains(addressJson.getCompanyName(),
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

