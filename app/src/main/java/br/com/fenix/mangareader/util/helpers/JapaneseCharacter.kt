package br.com.fenix.mangareader.util.helpers

/**
 * JapaneseCharacter contains static functions to do various tests
 * on characters to determine if it is one of the various types of
 * characters used in the japanese writing system.
 * <p/>
 * There are also a functions to translate between Katakana, Hiragana,
 * and Romaji.
 *
 * @author Duane J. May <djmay@mayhoo.com>
 * @version $Id: JapaneseCharacter.java,v 1.2 2002/04/20 18:10:24 djmay Exp $
 * @since 10:37 AM - 6/3/14
 *
 * @see <a href="http://sourceforge.net/projects/kanjixml/">http://sourceforge.net/projects/kanjixml/</a>
 */
class JapaneseCharacter {
    companion object {
        /**
         * Version information
         */
        private val VERSION = "\$Id: JapaneseCharacter.java,v 1.2 2002/04/20 18:10:24 djmay Exp $"

        /**
         * Determines if this character is a Japanese Kana.
         */
        fun isKana(c: Char): Boolean {
            return isHiragana(c) || isKatakana(c)
        }

        /**
         * Determines if this character is one of the Japanese Hiragana.
         */
        fun isHiragana(c: Char): Boolean {
            return '\u3041' <= c && c <= '\u309e'
        }

        /**
         * Determines if this character is one of the Japanese Katakana.
         */
        fun isKatakana(c: Char): Boolean {
            return isHalfWidthKatakana(c) || isFullWidthKatakana(c)
        }

        /**
         * Determines if this character is a Half width Katakana.
         */
        fun isHalfWidthKatakana(c: Char): Boolean {
            return c in '\uff66'..'\uff9d'
        }

        /**
         * Determines if this character is a Full width Katakana.
         */
        fun isFullWidthKatakana(c: Char): Boolean {
            return c in '\u30a1'..'\u30fe'
        }

        /**
         * Determines if this character is a Kanji character.
         */
        fun isKanji(c: Char): Boolean {
            if (c in '\u4e00'..'\u9fa5') {
                return true
            }
            return c in '\u3005'..'\u3007'
        }

        /**
         * Determines if this character could be used as part of
         * a romaji character.
         */
        fun isRomaji(c: Char): Boolean {
            return if ('\u0041' <= c && c <= '\u0090') true else if ('\u0061' <= c && c <= '\u007a') true else if ('\u0021' <= c && c <= '\u003a') true else if ('\u0041' <= c && c <= '\u005a') true else false
        }

        /**
         * Translates this character into the equivalent Katakana character.
         * The function only operates on Hiragana and always returns the
         * Full width version of the Katakana. If the character is outside the
         * Hiragana then the origianal character is returned.
         */
        fun toKatakana(c: Char): Char {
            return if (isHiragana(c)) {
                (c.toInt() + 0x60).toChar()
            } else c
        }

        /**
         * Translates this character into the equivalent Hiragana character.
         * The function only operates on Katakana characters
         * If the character is outside the Full width or Half width
         * Katakana then the origianal character is returned.
         */
        fun toHiragana(c: Char): Char {
            if (isFullWidthKatakana(c)) {
                return (c.toInt() - 0x60).toChar()
            } else if (isHalfWidthKatakana(c)) {
                return (c.toInt() - 0xcf25).toChar()
            }
            return c
        }

        /**
         * Translates this character into the equivalent Romaji character.
         * The function only operates on Hiragana and Katakana characters
         * If the character is outside the given range then
         * the origianal character is returned.
         *
         *
         * The resulting string is lowercase if the input was Hiragana and
         * UPPERCASE if the input was Katakana.
         */
        fun toRomaji(c: Char): String? {
            var c = c
            if (isHiragana(c)) {
                return lookupRomaji(c)
            } else if (isKatakana(c)) {
                c = toHiragana(c)
                val str = lookupRomaji(c)
                return str.toUpperCase()
            }
            return c.toString()
        }

        /**
         * The array used to map hirgana to romaji.
         */
        protected var romaji = arrayOf(
            "a", "a",
            "i", "i",
            "u", "u",
            "e", "e",
            "o", "o",
            "ka", "ga",
            "ki", "gi",
            "ku", "gu",
            "ke", "ge",
            "ko", "go",
            "sa", "za",
            "shi", "ji",
            "su", "zu",
            "se", "ze",
            "so", "zo",
            "ta", "da",
            "chi", "ji",
            "tsu", "tsu", "zu",
            "te", "de",
            "to", "do",
            "na",
            "ni",
            "nu",
            "ne",
            "no",
            "ha", "ba", "pa",
            "hi", "bi", "pi",
            "fu", "bu", "pu",
            "he", "be", "pe",
            "ho", "bo", "po",
            "ma",
            "mi",
            "mu",
            "me",
            "mo",
            "a", "ya",
            "u", "yu",
            "o", "yo",
            "ra",
            "ri",
            "ru",
            "re",
            "ro",
            "wa", "wa",
            "wi", "we",
            "o",
            "n",
            "v",
            "ka",
            "ke"
        )

        /**
         * Access the array to return the correct romaji string.
         */
        private fun lookupRomaji(c: Char): String {
            return romaji[c.toInt() - 0x3041]
        }
    }
}