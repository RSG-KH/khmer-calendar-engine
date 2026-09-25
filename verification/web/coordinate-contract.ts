import {calculateHoroscope, calculateHoroscopeUtc} from 'khmer-calendar-engine';
const local={year:2026,month:4,day:14,hour:10,minute:30,utcOffsetHours:7};
const utc={yearUtc:2026,monthUtc:4,dayUtc:14,hourUtc:3,minuteUtc:30};
for (const coords of [
 {latitude:11,longitude:104},
 {latitudeDeg:11,longitudeDeg:104},
 {latitude:11,longitudeDeg:104},
 {latitudeDeg:11,longitude:104},
]) {
 calculateHoroscope({...local,...coords});
 calculateHoroscopeUtc({...utc,...coords});
}
// @ts-expect-error Both coordinate axes are required.
calculateHoroscope(local);
// @ts-expect-error Longitude is required.
calculateHoroscope({...local,latitude:11});
// @ts-expect-error Latitude is required.
calculateHoroscope({...local,longitudeDeg:104});
// @ts-expect-error Both coordinate axes are required.
calculateHoroscopeUtc(utc);
// @ts-expect-error Longitude is required.
calculateHoroscopeUtc({...utc,latitudeDeg:11});
// @ts-expect-error Latitude is required.
calculateHoroscopeUtc({...utc,longitude:104});
