package ch.nexown.adresscrawler.service;

import ch.nexown.adresscrawler.model.Address;
import ch.nexown.adresscrawler.service.implementation.FileWriterServiceImpl;
import org.assertj.core.groups.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class FileWriterServiceTest {

    private static final String ADDRESSESNAMETEST = "TestAdresses";

    @InjectMocks
    private FileWriterService classUnderTest = new FileWriterServiceImpl();

    Address erniAdressTestData = AdressObjectMother.GetErniAdress();


    @Test
    public void writeJsons_with_valid_adress_object_does_not_fail_and_writes_json(){
        List<Address> erniAdressListTestData = Collections.singletonList(erniAdressTestData);

        classUnderTest.writeToJson(erniAdressListTestData, ADDRESSESNAMETEST);

        List<Address> result = classUnderTest.readAddressesFromJson(ADDRESSESNAMETEST);

        assertThat(result).extracting(
                Address::getCompanyName,
                Address::getId,
                Address::getuId)
                .containsExactly(Tuple.tuple(
                        erniAdressTestData.getCompanyName(),
                        erniAdressTestData.getId(),
                        erniAdressTestData.getuId()));

        assertThat(result.get(0).getAddress().getTown())
                .isEqualTo(String.valueOf(erniAdressTestData.getAddress().getTown()));
        assertThat(result.get(0).getAddress().getCountry())
                .isEqualTo(String.valueOf(erniAdressTestData.getAddress().getCountry()));
        assertThat(result.get(0).getAddress().getHouseNumber())
                .isEqualTo(String.valueOf(erniAdressTestData.getAddress().getHouseNumber()));
        assertThat(result.get(0).getAddress().getStreet())
                .isEqualTo(String.valueOf(erniAdressTestData.getAddress().getStreet()));
        assertThat(result.get(0).getAddress().getSwissZipCode())
                .isEqualTo(erniAdressTestData.getAddress().getSwissZipCode());
    }

    @Test
    public void writecsv_with_valid_adress_object_does_not_fail_and_writes_csv(){
        List<Address> erniAdressListTestData = Collections.singletonList(erniAdressTestData);

        classUnderTest.writeToCSV(erniAdressListTestData, ADDRESSESNAMETEST);

        List<Address> result = classUnderTest.readAddressesFromCSV(ADDRESSESNAMETEST);

        assertThat(result).extracting(
                Address::getCompanyName,
                Address::getId,
                Address::getuId)
                .containsExactly(Tuple.tuple(
                        erniAdressTestData.getCompanyName(),
                        erniAdressTestData.getId(),
                        erniAdressTestData.getuId()));

        assertThat(result.get(0).getAddress().getTown())
                .isEqualTo(String.valueOf(erniAdressTestData.getAddress().getTown()));
        assertThat(result.get(0).getAddress().getCountry())
                .isEqualTo(String.valueOf(erniAdressTestData.getAddress().getCountry()));
        assertThat(result.get(0).getAddress().getHouseNumber())
                .isEqualTo(String.valueOf(erniAdressTestData.getAddress().getHouseNumber()));
        assertThat(result.get(0).getAddress().getStreet())
                .isEqualTo(String.valueOf(erniAdressTestData.getAddress().getStreet()));
        assertThat(result.get(0).getAddress().getSwissZipCode())
                .isEqualTo(erniAdressTestData.getAddress().getSwissZipCode());
    }

}
