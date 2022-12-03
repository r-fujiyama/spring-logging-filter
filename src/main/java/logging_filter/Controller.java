package logging_filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/")
public class Controller {

    @PostMapping
    public Hoge hoge(@RequestBody Hoge hoge) {
        log.info("INFO");
        log.warn("WARN");
        log.error("ERROR");
        log.debug("DEBUG");
        log.trace("TRACE");
        hoge.setValue("fuga");
        return hoge;
    }

    @Data
    public static class Hoge {
        private String value;
    }
}
