package com.kenzo.kenchantments.utils;

public class RomanNumerals {

    private static final String[] ROMAN_NUMERALS = {
            "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"
    };

    public static String toRoman(int number) {
        return (number > 0 && number <= 10) ? ROMAN_NUMERALS[number - 1] : String.valueOf(number);
    }
}
