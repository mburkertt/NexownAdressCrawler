package ch.nexown.adresscrawler.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Created by buma on 19.01.2017.
 */

@RestController
public class ExampleController {

    @Resource(name = "uncompleteExample")
    private String uncompleteExample;

    @RequestMapping("/")
    public String exampleController() {
        return "";
    }

}

