package com.medievalfactions.domain;

import java.util.List;
import static com.medievalfactions.domain.MFGameCatalog.*;

/** The custom 378-day calendar: six seasons, three 21-day months per season, eight-day week. */
public final class MFCalendar {
    public static final int DAYS_PER_MONTH = 21;
    public static final int MONTHS_PER_SEASON = 3;
    public static final int DAYS_PER_SEASON = DAYS_PER_MONTH * MONTHS_PER_SEASON;
    public static final int SEASONS_PER_YEAR = 6;
    public static final int DAYS_PER_YEAR = DAYS_PER_SEASON * SEASONS_PER_YEAR;

    private static final List<List<String>> MONTHS = List.of(
            List.of("Aural", "Floren", "Verdalia"),
            List.of("Solkar", "Ignara", "Bravium"),
            List.of("Caldor", "Fervia", "Dorania"),
            List.of("Ocren", "Hojaria", "Vesper"),
            List.of("Nivaria", "Glacien", "Umbriel"),
            List.of("Sombria", "Noctara", "Eterion"));

    private MFCalendar() {}

    public static Date fromAbsoluteDay(long absoluteDay) {
        if (absoluteDay < 0L) throw new IllegalArgumentException("absoluteDay must be non-negative");
        long year = absoluteDay / DAYS_PER_YEAR + 1L;
        int dayOfYear = (int) (absoluteDay % DAYS_PER_YEAR);
        int seasonIndex = dayOfYear / DAYS_PER_SEASON;
        int dayWithinSeason = dayOfYear % DAYS_PER_SEASON;
        int monthWithinSeason = dayWithinSeason / DAYS_PER_MONTH;
        int dayOfMonth = dayWithinSeason % DAYS_PER_MONTH + 1;
        int absoluteWeekday = (int) (absoluteDay % DayName.values().length);
        return new Date(year, Season.values()[seasonIndex], MONTHS.get(seasonIndex).get(monthWithinSeason),
                dayOfMonth, DayName.values()[absoluteWeekday], dayOfYear + 1);
    }

    public static Date fromMinecraftDayTime(long minecraftDayTime) {
        return fromAbsoluteDay(Math.max(0L, minecraftDayTime / 24_000L));
    }

    public record Date(long year, Season season, String month, int dayOfMonth,
                       DayName weekday, int dayOfYear) {}
}
