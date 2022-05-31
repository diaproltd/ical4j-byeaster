package hu.diapro.ical4j.transform.recurrence;

import static net.fortuna.ical4j.model.Recur.Frequency.YEARLY;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.diapro.ical4j.EasterHelper;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.NumberList;
import net.fortuna.ical4j.model.Recur.Frequency;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.transform.recurrence.AbstractDateExpansionRule;
import net.fortuna.ical4j.util.Dates;

/**
 * Applies BYEASTER rules specified in this Recur instance to the specified date
 * list. If no BYEASTER rules are
 * specified the date list is returned unmodified.
 * 
 * Implementation based mostly on ByYearDayRule.
 */
public class ByEasterRule extends AbstractDateExpansionRule {
	private static final long serialVersionUID = 1L;

	private transient Logger log = LoggerFactory.getLogger(ByEasterRule.class);

	private final NumberList easterDayList;

	public ByEasterRule(NumberList easterDayList, Frequency frequency) {
		super(frequency);
		this.easterDayList = easterDayList;
	}

	public ByEasterRule(NumberList easterDayList, Frequency frequency, Optional<WeekDay.Day> weekStartDay) {
		super(frequency, weekStartDay);
		this.easterDayList = easterDayList;
	}

	@Override
	public DateList transform(DateList dates) {
		if (easterDayList.isEmpty()) {
			return dates;
		}
		final DateList easterDayDates = Dates.getDateListInstance(dates);
		for (final Date date : dates) {
			if (getFrequency() == YEARLY) {
				easterDayDates.addAll(new ExpansionFilter(easterDayDates.getType()).apply(date));
			} else {
				Optional<Date> limit = new LimitFilter().apply(date);
				if (limit.isPresent()) {
					easterDayDates.add(limit.get());
				}
			}
		}
		return easterDayDates;
	}

	private final class LimitFilter implements Function<Date, Optional<Date>> {

		@Override
		public Optional<Date> apply(Date date) {
			final Calendar cal = getCalendarInstance(date, true);
			final LocalDate easterSunday = EasterHelper.getEasterSunday(cal.get(Calendar.YEAR));
			final int easterSundayDayOfYear = easterSunday.getDayOfYear();
			final int dateEasterDay = cal.get(Calendar.DAY_OF_YEAR) - easterSundayDayOfYear;

			if (easterDayList.contains(dateEasterDay)) {
				return Optional.of(date);
			}

			return Optional.empty();
		}
	}

	private final class ExpansionFilter implements Function<Date, List<Date>> {

		private final Value type;

		public ExpansionFilter(Value type) {
			this.type = type;
		}

		@Override
		public List<Date> apply(Date date) {
			final List<Date> result = new ArrayList<>();

			final Calendar cal = getCalendarInstance(date, false);
			final LocalDate easterSunday = EasterHelper.getEasterSunday(cal.get(Calendar.YEAR));
			final int easterSundayDayOfYear = easterSunday.getDayOfYear();
			final int numDaysInYear = cal.getActualMaximum(Calendar.DAY_OF_YEAR);
			final int minEasterDay = 1 - easterSundayDayOfYear;
			final int maxEasterDay = numDaysInYear - easterSundayDayOfYear;

			// construct a list of possible easter days..
			for (final int easterDay : easterDayList) {
				if (easterDay < -Dates.MAX_DAYS_PER_YEAR || easterDay > Dates.MAX_DAYS_PER_YEAR) {
                    if (log.isTraceEnabled()) {
                    	log.trace("Invalid day relative to easter sunday: " + easterDay);
                    }
                    continue;
                }
				
				if (easterDay < minEasterDay || easterDay > maxEasterDay) {
					// Not applicable for this year.
					continue;
				}

				cal.set(Calendar.DAY_OF_YEAR, easterSundayDayOfYear);
				cal.add(Calendar.DAY_OF_YEAR, easterDay);
				result.add(Dates.getInstance(getTime(date, cal), type));
			}

			return result;
		}
	}

	/**
	 * @param stream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		log = LoggerFactory.getLogger(ByEasterRule.class);
	}
}
