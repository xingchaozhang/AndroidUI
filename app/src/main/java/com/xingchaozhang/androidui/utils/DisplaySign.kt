package com.xingchaozhang.androidui.utils

import com.ibm.icu.number.NumberFormatter
enum class DisplaySign {
    /**
     * Show the minus sign on negative numbers, and do not show the sign on positive numbers.
     */
    AUTO,

    /**
     * Show the minus sign on negative numbers and the plus sign on positive numbers.
     * Do not show a sign on zero, numbers that round to zero, or NaN.
     */
    EXCEPT_ZERO,

    /**
     * Show the minus sign on negative numbers and the plus sign on positive numbers, including zero.
     */
    ALWAYS,;


    fun toSignDisplay(): NumberFormatter.SignDisplay {
        return  when (this) {
            AUTO -> {
                NumberFormatter.SignDisplay.AUTO
            }
            EXCEPT_ZERO -> {
                NumberFormatter.SignDisplay.EXCEPT_ZERO
            }
            ALWAYS -> {
                NumberFormatter.SignDisplay.ALWAYS
            }
        }
    }
}