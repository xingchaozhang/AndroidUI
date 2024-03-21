package com.xingchaozhang.androidui.ui.fragments

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.okinc.uilab.edit.LocalizationNumberEditText
import com.xingchaozhang.androidui.R
import com.xingchaozhang.androidui.databinding.FragmentLocalizationInputEditTextBinding
import java.util.*

/**
 * test cases:
 * integers：
 * 1234 1234002200 0000   01   00001234   12340000  000012340000  00123400
 *
 * regular decimals：
 * 0.1234            0,1234
 * 0.123400          0,123400
 * 0000.1234         0000,1234
 * 0000.12340000     0000,1234000
 * 000123.1112       000123,1112
 * 00001234.123400   00001234,123400
 * 1234.002200       1234,002200
 * 126.0000          126,0000
 * 0.4               0,4
 * 00.00             00,00
 * 999.100           999,100
 *
 * unregular decimals：
 * 00.00..00012    0,00,,00,,00,,12
 * 00.00.,.,.,.,.
 * 12645.000.,.,.,..
 * 00012345..,..2656.1545
 * .000.00.00.00
 * ,123456,,2200
 * ,00123456,5000
 * 000123,123,123,1546,,365 00123.123.123
 * .4          ,4
 * .400        ,400
 * .4000200    ,400200
 *
 * repeat paste：
 *
 * 123        123
 * 123.2      123,3
 * 123.5      123
 * 125.254    002231
 * 123.56     000,0023
 * 0          .123       00,123
 * 0          00.21500   00,21500
 *
 * contains invalid characters
 * #$%^&*
 * FGUIJ_()U
 * 564165.03sdfgkosdjf
 *
 * 在土耳其语，开启千分位的情况下，测试以下内容
 * editText.plainNumbericText = "123456.00";
 * 此时输入框显示"123.456,00"
 * print(editText.plainNumericText) // 这里也需要打印出"123456.00"
 * 在土耳其语，开启千分位情况下，测试以下内容
 * editText.localizedNumericText = "123.456,00";
 * 此时输入框显示“123.456,00”
 * print(editText.localizedNumericText) // 这里打印出123.456,00
 * 12340.0
 */
class LocalizationInputEditTextFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentLocalizationInputEditTextBinding
    private var inputEditText: LocalizationNumberEditText? = null
    private var inputString = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
//        Locale.setDefault(Locale("ru", "RU"))
//        Locale.setDefault(Locale.ITALIAN)
//        Locale.setDefault(Locale("ar", "eh"))
        binding = FragmentLocalizationInputEditTextBinding.inflate(inflater, container, false)
        binding.btnRandom.setOnClickListener(this)
        binding.btnPlain.setOnClickListener(this)
        binding.btnLocalized.setOnClickListener(this)

        binding.edtRandomText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                inputString = s.toString()
                binding.edtPlainText.plainNumericText = inputString
                binding.edtLocalizedText.localizedNumericText =
                    binding.edtPlainText.localizedNumericText
                println("zxc binding.edtPlainText.plainNumericText = " + binding.edtPlainText.plainNumericText)
                println("zxc binding.edtLocalizedText.localizedNumericText = " + binding.edtLocalizedText.localizedNumericText)
            }
        })

        inputEditText?.apply {
            showGroupingSeparator = false
//            locale = Locale.ITALIAN
//            maxInputLength = 5
//            maxDecimalValue = 2
        }
        binding.edtPlainText.apply {
            showGroupingSeparator = true
//            legalCharacters = setOf('#', '%')
//            maxInputLength = 5
//            maxDecimalValue = 2
        }
        binding.edtLocalizedText.apply {
            showGroupingSeparator = true
//            maxInputLength = 5
//            maxDecimalValue = 2
        }
//        edtPlainText?.removeFilters()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_random -> {
                val r = Random()
                val d2 = r.nextFloat() * 20000
                inputString =
                    d2.toString()/* + StrFormatUtil.TAG_STR_FORMAT + StrFormatUtil.TAG_STR_ERROR*/
                binding.edtRandomText.setText(inputString)
            }

            R.id.btn_plain -> {
                inputEditText?.plainNumericText = inputString
                println("zxc binding.edtPlainText.plainNumericText = " + binding.edtPlainText.plainNumericText)
            }

            R.id.btn_localized -> {
                inputEditText?.localizedNumericText = inputString
            }
        }
    }
}