package hu.diapro.ical4j;

import static java.time.Month.APRIL;
import static java.time.Month.MARCH;

import java.time.LocalDate;
import java.time.temporal.ValueRange;

import org.threeten.extra.chrono.JulianChronology;

public class EasterHelper {
	/**
	 * Returns the easter sunday for a given year.
	 *
	 * @param year a int.
	 * @return Easter sunday.
	 */
	public static LocalDate getEasterSunday(int year) {
		if (year <= 1583) {
			return getJulianEasterSunday(year);
		} else {
			return getGregorianEasterSunday(year);
		}
	}

	/**
	 * Returns the easter sunday within the julian chronology.
	 *
	 * @param year a int.
	 * @return julian easter sunday
	 */
	public static LocalDate getJulianEasterSunday(int year) {
		int a, b, c, d, e;
		int x, month, day;
		a = year % 4;
		b = year % 7;
		c = year % 19;
		d = (19 * c + 15) % 30;
		e = (2 * a + 4 * b - d + 34) % 7;
		x = d + e + 114;
		month = x / 31;
		day = (x % 31) + 1;
		return LocalDate.from(JulianChronology.INSTANCE.date(year, (month == 3 ? 3 : 4), day));
	}

	/**
	 * Returns the easter sunday within the gregorian chronology.
	 *
	 * @param year a int.
	 * @return gregorian easter sunday.
	 */
	public static LocalDate getGregorianEasterSunday(int year) {
		int a, b, c, d, e, f, g, h, i, j, k, l;
		int x, month, day;
		a = year % 19;
		b = year / 100;
		c = year % 100;
		d = b / 4;
		e = b % 4;
		f = (b + 8) / 25;
		g = (b - f + 1) / 3;
		h = (19 * a + b - d - g + 15) % 30;
		i = c / 4;
		j = c % 4;
		k = (32 + 2 * e + 2 * i - h - j) % 7;
		l = (a + 11 * h + 22 * k) / 451;
		x = h + k - 7 * l + 114;
		month = x / 31;
		day = (x % 31) + 1;
		return LocalDate.of(year, (month == 3 ? MARCH : APRIL), day);
	}

	public static ValueRange validEasterDayValues() {
		// 284 is the difference between 12.31. and 03.22. (the earliest possible day of
		// Easter Sunday).
		return ValueRange.of(0, 284);
	}
}
