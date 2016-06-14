package com.dandersen.app.easyshoppinglist.utils;

import android.content.Context;
import android.support.annotation.Nullable;

import com.dandersen.app.easyshoppinglist.R;

import java.text.DateFormatSymbols;
import java.util.Calendar;

/**
 * Created by Dan on 09-06-2016.
 * Utitlities for formatting strings in different ways.
 * - Address
 * - Opening hours
 */
public class StringUtil {

    public static class FormattedAddress {

        public String mStreet;
        public String mStreetNumber;
        public String mPostalCode;
        public String mCity;
        public String mState;
        public String mCountry;
        private String mSep = ", ";

        FormattedAddress(@Nullable String formattedAddress) {
            if (formattedAddress == null) return;
            String[] parts = formattedAddress.split(",");
            if (parts.length > 0) {
                String streetAndNumberStr = parts[0];
                String[] streetAndNumber = streetAndNumberStr.split(" ");
                if (streetAndNumber.length > 1) {
                    mStreetNumber = streetAndNumber[streetAndNumber.length - 1];
                    mStreet = streetAndNumberStr.substring(0, streetAndNumberStr.length() - mStreetNumber.length() - 1);
                }
            }
            if (parts.length > 1) {
                String postalCodeAndCityStr = parts[1];
                String[] postalCodeAndCity = postalCodeAndCityStr.split(" ");
                mPostalCode = postalCodeAndCity[0];
                if (postalCodeAndCity.length > 1) {
                    mCity = postalCodeAndCity[1];
                }
            }
            if (parts.length > 2) {
                mCountry = parts[2];
            }
        }

        private void addSeparator(StringBuilder sb) {
            if (sb.length() != 0) {
                sb.append(mSep);
            }
        }

        private void addValue(StringBuilder sb, String val, boolean addSep) {
            if (val != null && !val.isEmpty()) {
                if (addSep) {
                    addSeparator(sb);
                }
                sb.append(val);
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (!mStreet.isEmpty()) {
                addValue(sb, mStreet, false);
                sb.append(" ");
            }
            addValue(sb, mStreetNumber, false);
            if (!mPostalCode.isEmpty()) {
                addValue(sb, mPostalCode, true);
                sb.append(" ");
                addValue(sb, mCity, false);
            }
            else {
                addValue(sb, mCity, true);
            }
            addValue(sb, mState, true);
            addValue(sb, mCountry, true);
            return sb.toString();
        }
    }

    public static FormattedAddress formatAddress(String formattedAddress) {
        return new FormattedAddress(formattedAddress);
    }

    public static FormattedAddress formattedAddress(String street,
                                                    String streetNumber,
                                                    String postalCode,
                                                    String city,
                                                    String state,
                                                    String country) {
        FormattedAddress formattedAddress = new FormattedAddress(null);
        formattedAddress.mStreet = street;
        formattedAddress.mStreetNumber = streetNumber;
        formattedAddress.mPostalCode = postalCode;
        formattedAddress.mCity = city;
        formattedAddress.mState = state;
        formattedAddress.mCountry = country;
        return formattedAddress;
    }

    public static String buildShopAddress(String street,
                                          String streetNumber,
                                          String city) {
        final String SPACE_SEP = " ";
        final String COMMA_SEP = ", ";

        String res = "";
        if (street != null) {
            res = street;
        }
        if (streetNumber != null) {
            if (!res.isEmpty()) res += SPACE_SEP;
            res += streetNumber;
        }
        if (city != null) {
            if (!res.isEmpty()) res += COMMA_SEP;
            res += city;
        }
        return res;
    }

    public static String OPEN_CLOSE_SEP         = "$";
    public static String DAY_TIME_SEP           = "-";
    public static String DAY_SEP                = ";";

    /**
     * Return display text for opening hours
     * @param context The activity context from caller
     * @param str The encoded opening hours text eg: 0-0700$0-2000;1-0700$1-2000
     * @return Display text where item 1 is the days string and item 2 is the hours string.
     *         Current day is marked with red color using HTML codes
     */
    public static String[] buildOpeningHours(Context context, String str) {
        final String SPACE_SEP              = " ";
        final String HYPHEN_SEP             = "-";
        final String[] NAMES_OF_DAYS        = DateFormatSymbols.getInstance().getWeekdays();
        final String HTML_COLOR_START       = "<font color=#ff0000>";
        final String HTML_COLOR_END         = "</font>";
        final String HTML_RETURN            = "<br>";

        StringBuilder sbDays = new StringBuilder();
        StringBuilder sbHours = new StringBuilder();

        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

        String[] days = str.split(DAY_SEP);
        for (String day : days) {
            String[] openClose = day.split("\\" + OPEN_CLOSE_SEP);
            String[] openDayTime = openClose[0].split(DAY_TIME_SEP);

            // Add day name
            String dayStr = openDayTime[0];
            String openTimeStr = openDayTime[1];
            int dayOfWeek = getCalendarDayOfWeek(Integer.valueOf(dayStr));
            if (dayOfWeek == currentDay) {
                sbDays.append(HTML_COLOR_START);
                sbHours.append(HTML_COLOR_START);
            }
            sbDays.append(NAMES_OF_DAYS[dayOfWeek]);

            // Handle always open case
            if (openClose.length == 1) {
                sbHours.append(context.getString(R.string.opening_hours_open_until));
                sbHours.append(SPACE_SEP);
                sbHours.append(formatTime(openTimeStr));
            }
            else {
                // Add opening time
                sbHours.append(formatTime(openTimeStr));

                // Add closing time
                String[] closeDayTime = openClose[1].split(DAY_TIME_SEP);
                String closeTimeStr = closeDayTime[1];
                sbHours.append(HYPHEN_SEP);
                sbHours.append(formatTime(closeTimeStr));
            }
            if (dayOfWeek == currentDay) {
                sbDays.append(HTML_COLOR_END);
                sbHours.append(HTML_COLOR_END);
            }
            sbDays.append(HTML_RETURN);
            sbHours.append(HTML_RETURN);
        }

        return new String[] { sbDays.toString() , sbHours.toString() };
    }

    private static int getCalendarDayOfWeek(int dayNumberFromGoogle) {
        switch (dayNumberFromGoogle) {
            case 0:     return Calendar.SUNDAY;
            case 1:     return Calendar.MONDAY;
            case 2:     return Calendar.TUESDAY;
            case 3:     return Calendar.WEDNESDAY;
            case 4:     return Calendar.THURSDAY;
            case 5:     return Calendar.FRIDAY;
            case 6:     return Calendar.SATURDAY;
            default:
                throw new AssertionError("Unhandled day number " + dayNumberFromGoogle);
        }
    }

    private static String formatTime(String timeStr) {
        if (timeStr.length() == 4) {
            if (timeStr.charAt(3) == '0' && timeStr.charAt(2) == '0') {
                return timeStr.substring(0, 2);
            }
        }
        return timeStr;
    }
}
