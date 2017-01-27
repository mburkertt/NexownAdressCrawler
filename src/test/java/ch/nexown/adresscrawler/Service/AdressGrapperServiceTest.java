package ch.nexown.adresscrawler.Service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

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
    public void webServiceCall_delivers_addresses_safed_as_file() {
        grapperService.webServiceCall();
    }

    @Test
    public void getInformation_from_webservice_test_request() {
        grapperService.getInformation(1);
    }

}
