// Copyright (c) 2026 RSG-KH | Apache-2.0 License
// Calls the existing Android implementation; contains no calendar algorithms.
import com.rsgkh.calendar.data.RecurringEvents;
import com.rsgkh.calendar.domain.KhmerCalendar;
import com.rsgkh.calendar.domain.KhmerDateDetails;
import com.rsgkh.calendar.domain.KhmerNewYear;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

class ExportAndroidBaseline {
    public static void main(String[] args) throws Exception {
        var rows = new ArrayList<String>();
        for (var date = LocalDate.of(1800, 1, 1); date.isBefore(LocalDate.of(2201, 1, 1)); date = date.plusDays(1)) {
            var details = KhmerDateDetails.Companion.fromGregorian(date);
            var lunar = details.getLunar();
            rows.add(String.join("\t", "D", date.toString(), Integer.toString(lunar.getDay()),
                Boolean.toString(lunar.getWaxing()), Integer.toString(lunar.getMonth()),
                Integer.toString(lunar.getBuddhistYear()), Integer.toString(lunar.getMonthLength()),
                Boolean.toString(lunar.isHolyDay()), Boolean.toString(lunar.isShavingDay()),
                Integer.toString(details.getAnimalYear()), Integer.toString(details.getSak()),
                Boolean.toString(details.getAnimalYearChangesToday()), details.getZodiac().name()));
        }
        for (int year = 1800; year <= 2200; year++) {
            var newYear = KhmerNewYear.INSTANCE.forYear(year, KhmerCalendar.INSTANCE);
            rows.add("N\t" + year + "\t" + newYear.getStart() + "\t" + newYear.getDays());
            for (var event : RecurringEvents.INSTANCE.forYear(year)) {
                rows.add("R\t" + year + "\t" + event.getId() + "\t" + event.getDate());
            }
        }
        Collections.sort(rows);
        Files.writeString(Path.of(args[0]), String.join("\n", rows) + "\n", StandardCharsets.UTF_8);
    }
}
