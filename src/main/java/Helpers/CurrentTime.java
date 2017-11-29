package Helpers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CurrentTime {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
    LocalDate localDate = LocalDate.now();

    public String get() {
        return this.dtf.format(this.localDate);
    }
}