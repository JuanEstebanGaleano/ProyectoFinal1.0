package ziploc.ZiplocSAS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ZiplocApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZiplocApplication.class, args);
    }
}