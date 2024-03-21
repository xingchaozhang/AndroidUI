package com.xingchaozhang.androidui.utils

import java.math.BigDecimal
import kotlin.math.abs


class Precision private constructor(
    private val type: PrecisionType,
    private val fraction1: Int,
    private val fraction2: Int,
    private val roundingIncrement: BigDecimal,
) {
    private val TAG = "Precision"

    companion object {
        /**
         * min precision, eg, minPrecision(2): 3.14159 -> 3.14159, 3 -> 3.00
         * minFraction must >= 0
         */
        fun minPrecision(minFraction: Int): Precision {
            return Precision(PrecisionType.MIN, minFraction, 0, BigDecimal.ZERO)
        }

        /**
         * max precision, eg, maxPrecision(2): 3.14159 -> 3.14, 3 -> 3
         * maxFraction must >= 0
         */
        fun maxPrecision(maxFraction: Int): Precision {
            return Precision(PrecisionType.MAX, maxFraction, 0, BigDecimal.ZERO)
        }

        /**
         *  min and max precision, eg, minMaxPrecision(1,2): 3.14159 -> 3.14, 3 -> 3.0
         *  minFraction and maxFraction must >= 0
         */
        fun minMaxPrecision(minFraction: Int, maxFraction: Int): Precision {
            return Precision(PrecisionType.MIN_MAX, minFraction, maxFraction, BigDecimal.ZERO)
        }

        /**
         * fixed precision. eg, fixedPrecision(2): 3.14159 -> 3.14, 3 -> 3.00
         * fixedPrecision must >= 0
         */
        fun fixedPrecision(fixedPrecision: Int): Precision {
            return Precision(PrecisionType.FIXED, fixedPrecision, 0, BigDecimal.ZERO)
        }

        /**
         * increment precision. roundingIncrement must > 0
         * eg1: increment(BigDecimal("0.5")) :     1.6 -> 1.5
         * eg2: increment(BigDecimal("10.00")) :   123.45 -> 120.00
         *
         * Show numbers rounded if necessary to the closest multiple of a certain rounding increment.
         * For example, if the rounding increment is 0.5, then round 1.2 to 1 and round 1.3 to 1.5.
         * In order to ensure that numbers are padded to the appropriate number of fraction places, set the scale on the rounding increment BigDecimal.
         * For example, to round to the nearest 0.5 and always display 2 numerals after the decimal separator (to display 1.2 as "1.00" and 1.3 as "1.50"), you can run:
         *   Precision.increment(BigDecimal("0.50"))
         */
        fun increment(roundingIncrement: BigDecimal): Precision {
            return Precision(PrecisionType.INCREMENT, 0, 0, roundingIncrement)
        }

        // compat the negative precision.
        internal fun compatNegativePrecision(fixedPrecision: Int): Precision {
            require(fixedPrecision < 0)
            val powNum = abs(fixedPrecision)

            val roundingIncrement = BigDecimal(10).pow(powNum)
            return increment(roundingIncrement)
        }
    }

    internal fun toICUPrecision(): com.ibm.icu.number.Precision {
        return when (type) {
            PrecisionType.MIN -> {
                com.ibm.icu.number.Precision.minFraction(fraction1)
            }

            PrecisionType.MAX -> {
                com.ibm.icu.number.Precision.maxFraction(fraction1)
            }

            PrecisionType.MIN_MAX -> {
                if (fraction1 > fraction2) {
                    return com.ibm.icu.number.Precision.fixedFraction(fraction1)
                }
                return com.ibm.icu.number.Precision.minMaxFraction(fraction1, fraction2)
            }

            PrecisionType.FIXED -> {
                com.ibm.icu.number.Precision.fixedFraction(fraction1)
            }

            PrecisionType.INCREMENT -> {
                com.ibm.icu.number.Precision.increment(roundingIncrement)
            }
        }
    }
}

internal enum class PrecisionType {
    MIN,
    MAX,
    MIN_MAX,
    FIXED,
    INCREMENT
}