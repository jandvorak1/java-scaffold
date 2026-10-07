/**
 * Defines the separators and fractional precision used to display decimal values.
 */
export type DecimalFormatOptions = Readonly<{

    /** Single character placed before the fractional part. */
    decimalSeparator: string;

    /**
     * Optional single character used to separate groups of three integer digits.
     *
     * Null, an empty string, or the null character disables grouping.
     */
    groupingSeparator?: string | null;

    /** Minimum number of digits retained in the fractional part. */
    minimumFractionDigits: number;

    /** Maximum number of digits retained in the fractional part. */
    maximumFractionDigits: number;
}>;

/**
 * Formats a technical date using a Java date-time pattern supported by this module.
 *
 * Supported pattern tokens are yyyy, MM, and dd. Literal text can be enclosed
 * in apostrophes. The original value is returned for an invalid calendar date
 * or unsupported pattern.
 *
 * @param value Technical date in yyyy-MM-dd format.
 * @param pattern Pattern used to produce the displayed date.
 * @returns The formatted date, or the original value when formatting fails.
 */
export function formatDate(value: string, pattern: string): string {
    const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);
    if (!match) {
        return value;
    }

    const [, year, month, day] = match;
    if (!isValidDate(year, month, day)) {
        return value;
    }
    return applyPattern(pattern, {
        yyyy: year,
        MM: month,
        dd: day,
    }, value);
}

/**
 * Formats a technical time using a Java date-time pattern supported by this module.
 *
 * Supported pattern tokens are HH, h, mm, ss, and a. Literal text can be
 * enclosed in apostrophes. The original value is returned for an invalid time
 * or unsupported pattern.
 *
 * @param value Technical time in HH:mm:ss format.
 * @param pattern Pattern used to produce the displayed time.
 * @returns The formatted time, or the original value when formatting fails.
 */
export function formatTime(value: string, pattern: string): string {
    const match = /^(\d{2}):(\d{2}):(\d{2})$/.exec(value);
    if (!match) {
        return value;
    }

    const [, hour, minute, second] = match;
    if (!isValidTime(hour, minute, second)) {
        return value;
    }

    const hourNumber = Number(hour);
    const hour12 = hourNumber % 12 || 12;
    return applyPattern(pattern, {
        HH: hour,
        h: String(hour12),
        mm: minute,
        ss: second,
        a: hourNumber < 12 ? "AM" : "PM",
    }, value);
}

/**
 * Formats a technical date-time using a Java date-time pattern supported by this module.
 *
 * Supported pattern tokens are yyyy, MM, dd, HH, h, mm, ss, and a. Literal text
 * can be enclosed in apostrophes. The original value is returned for an invalid
 * date-time or unsupported pattern.
 *
 * @param value Technical date-time in yyyy-MM-dd'T'HH:mm:ss format.
 * @param pattern Pattern used to produce the displayed date-time.
 * @returns The formatted date-time, or the original value when formatting fails.
 */
export function formatDateTime(value: string, pattern: string): string {
    const match = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})$/.exec(value);
    if (!match) {
        return value;
    }

    const [, year, month, day, hour, minute, second,] = match;
    if (!isValidDate(year, month, day) || !isValidTime(hour, minute, second)) {
        return value;
    }

    const hourNumber = Number(hour);
    const hour12 = hourNumber % 12 || 12;
    return applyPattern(pattern, {
        yyyy: year,
        MM: month,
        dd: day,
        HH: hour,
        h: String(hour12),
        mm: minute,
        ss: second,
        a: hourNumber < 12 ? "AM" : "PM",
    }, value);
}

/**
 * Formats a technical decimal value using the supplied display options.
 *
 * The input must use a dot as its decimal separator and cannot contain grouping
 * separators. Values are rounded with the half-up strategy used by the backend.
 * The original value is returned when the input or options are invalid.
 *
 * @param value Technical decimal value to format.
 * @param format Separators and fractional precision used for display.
 * @returns The formatted decimal value, or the original value when formatting fails.
 */
export function formatDecimal(value: string | number, format: DecimalFormatOptions): string {
    const originalValue = String(value);
    const match = /^([+-]?)(\d+)(?:\.(\d+))?$/.exec(originalValue.trim());
    if (!match) {
        return originalValue;
    }

    const minimumFractionDigits = format.minimumFractionDigits;
    const maximumFractionDigits = format.maximumFractionDigits;
    if (!Number.isInteger(minimumFractionDigits)
        || !Number.isInteger(maximumFractionDigits)
        || minimumFractionDigits < 0
        || maximumFractionDigits < minimumFractionDigits
        || format.decimalSeparator.length !== 1
        || !isValidGroupingSeparator(format.groupingSeparator)) {
        return originalValue;
    }

    const sign = match[1] ?? "";
    const integer = match[2];
    const fraction = match[3] ?? "";
    if (integer === undefined) {
        return originalValue;
    }

    const rounded = roundDecimal(integer, fraction, maximumFractionDigits);
    let integerPart = rounded.integer;
    let fractionPart = rounded.fraction;

    while (fractionPart.length > minimumFractionDigits && fractionPart.endsWith("0")) {
        fractionPart = fractionPart.slice(0, -1);
    }

    const groupingSeparator = format.groupingSeparator;
    if (groupingSeparator && groupingSeparator !== "\0") {
        integerPart = applyGrouping(integerPart, groupingSeparator);
    }

    const decimalPart = fractionPart ? format.decimalSeparator + fractionPart : "";
    return sign + integerPart + decimalPart;
}

function applyPattern(pattern: string, values: Record<string, string | undefined>, fallback: string): string {
    const tokens = ["yyyy", "HH", "MM", "dd", "mm", "ss", "h", "a"];
    let formatted = "";
    let quoted = false;
    for (let index = 0; index < pattern.length;) {
        if (pattern[index] === "'") {
            if (pattern[index + 1] === "'") {
                formatted += "'";
                index += 2;
            } else {
                quoted = !quoted;
                index++;
            }
            continue;
        }
        if (quoted) {
            formatted += pattern[index];
            index++;
            continue;
        }
        const token = tokens.find(candidate => pattern.startsWith(candidate, index));
        if (token) {
            const value = values[token];
            if (value === undefined) {
                return fallback;
            }
            formatted += value;
            index += token.length;
            continue;
        }
        const character = pattern[index];
        if (character === undefined || /[A-Za-z]/.test(character)) {
            return fallback;
        }
        formatted += character;
        index++;
    }
    return quoted ? fallback : formatted;
}

function applyGrouping(value: string, separator: string): string {
    return value.replace(/\B(?=(\d{3})+(?!\d))/g, () => separator);
}

function roundDecimal(integer: string, fraction: string, maximumFractionDigits: number): {
    integer: string;
    fraction: string;
} {
    const retainedFraction = fraction.slice(0, maximumFractionDigits).padEnd(maximumFractionDigits, "0");
    const scale = 10n ** BigInt(maximumFractionDigits);
    let scaledValue = BigInt(integer) * scale;
    if (retainedFraction) {
        scaledValue += BigInt(retainedFraction);
    }

    const roundingDigit = fraction[maximumFractionDigits];
    if (roundingDigit !== undefined && roundingDigit >= "5") {
        scaledValue++;
    }

    const roundedInteger = scaledValue / scale;
    const roundedFraction = maximumFractionDigits === 0
        ? ""
        : String(scaledValue % scale).padStart(maximumFractionDigits, "0");
    return {
        integer: String(roundedInteger), fraction: roundedFraction,
    };
}

function isValidDate(year: string | undefined, month: string | undefined, day: string | undefined): boolean {
    const yearNumber = Number(year);
    const monthNumber = Number(month);
    const dayNumber = Number(day);
    if (monthNumber < 1 || monthNumber > 12 || dayNumber < 1) {
        return false;
    }
    const leapYear = yearNumber % 4 === 0 && (yearNumber % 100 !== 0 || yearNumber % 400 === 0);
    const daysInMonth = [31, leapYear ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
    return dayNumber <= (daysInMonth[monthNumber - 1] ?? 0);
}

function isValidTime(hour: string | undefined, minute: string | undefined, second: string | undefined): boolean {
    return Number(hour) <= 23 && Number(minute) <= 59 && Number(second) <= 59;
}

function isValidGroupingSeparator(separator: string | null | undefined): boolean {
    return separator === undefined || separator === null || separator.length <= 1;
}
