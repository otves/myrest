package hello;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;
import java.util.Map;

/**
 * Created by otves on 04.09.2016.
 */
@Controller
public class WebController {
    /**
     * Html страница по умолчанию.
     *
     * @return имя JSP страницы.
     */

    @GetMapping("/")
    public String welcome(Map<String, Object> model) {
        model.put("time", new Date());
        return "sample";
    }
}
