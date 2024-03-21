package com.xingchaozhang.androidui.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xingchaozhang.androidui.R
import com.xingchaozhang.androidui.databinding.FragmentInputBinding
import com.xingchaozhang.androidui.edit.input.Input
import com.xingchaozhang.androidui.edit.input.Input.Companion.FIELD_LABEL_INSIDE
import com.xingchaozhang.androidui.edit.input.Input.Companion.FIELD_LABEL_OUTSIDE

/**
 * @date : 2022/11/3 14:49
 * @author ： zhangxingchao
 * @description : Demo for Input
 */
class InputFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentInputBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInputBinding.inflate(inflater, container, false)
        binding.apply {
            okInputField1.apply {
                setLabelText("Label")
                setTitleType(FIELD_LABEL_INSIDE)
                contentView?.contentBinding?.edtContent?.setText("1234")
                val view1 = getView(contentView?.contentBinding?.llFunction)
                val view2 = getView(contentView?.contentBinding?.llFunction)
                contentView?.replaceFunctionView(view2)
            }
            okInputField2.apply {
                setLabelText("Label")
                setTitleType(FIELD_LABEL_OUTSIDE)
                contentView?.contentBinding?.edtContent?.setText("1234")
                contentView?.replaceFunctionView(getView(contentView?.contentBinding?.llFunction))
            }
            okInputField3.apply {
                setLabelText("Label")
//                setSizeType(Input.ContentHeight.XL)
                contentView?.contentBinding?.edtContent?.setText("1234")
                contentView?.replaceFunctionView(getView(contentView?.contentBinding?.llFunction))
            }
            okInputField4.apply {
                setLabelText("Label")
//                setState(3)
                showClearButtonWhenEditing(false)
                contentView?.contentBinding?.edtContent?.setText("1234")
            }
            okInputField41.apply {
                setLabelText("Label")
                contentView?.contentBinding?.edtContent?.setText("1234")
                contentView?.replaceFunctionView(getView(contentView?.contentBinding?.llFunction))
            }

            okInputField11.apply {
                setLabelText("Label")
                contentView?.contentBinding?.edtContent?.setText("1234")
//                contentView?.replaceFunctionView(getView(contentView?.contentBinding?.llFunction))
            }
            okInputField12.apply {
                setLabelText("Label")
//                contentView?.contentBinding?.edtContent?.setText("1234")
                contentView?.replaceFunctionView(getView(contentView?.contentBinding?.llFunction))
                setHintText("shpahdfpajpdfjaopdfhopisdafpoadjfopasdfdsf")
            }
            okInputField13.apply {
                setLabelText("Label")
                contentView?.contentBinding?.edtContent?.setText("1234")
                contentView?.replaceFunctionView(getView(contentView?.contentBinding?.llFunction))
            }
        }

        binding.btnSetLabelText.setOnClickListener(this)
        binding.btnReplaceFunction.setOnClickListener(this)
        binding.btnAddHelper.setOnClickListener(this)
        binding.btnSetError.setOnClickListener(this)
        binding.btnClearError.setOnClickListener(this)
        binding.btnDisplayNumber.setOnClickListener(this)
        binding.btnEnable.setOnClickListener(this)
        binding.btnDisable.setOnClickListener(this)
        binding.btnReset.setOnClickListener(this)

        setInputFieldFunction()
        binding.input.contentView?.validator = {
            it?.length?.run {
                if (this > 8) {
                    "Input amount beyond 8."
                } else {
                    ""
                }
            }
        }
        binding.input.contentView?.onFocusChangeCallback = {
            println("zxc  it = $it")
        }
        binding.input.contentView?.contentBinding?.edtContent?.hint = "123456789"
        binding.input.setState(Input.TYPE_STATIC)
        binding.highLightInput.contentView?.validator = {
            it?.length?.run {
                if (this > 8) {
                    "Input amount beyond 8."
                } else {
                    ""
                }
            }
        }
        return binding.root
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_set_label_text -> {
                setLabelText()
                binding.input.getContentFunctionViewGroup()?.visibility = View.GONE
                binding.okInputField2.setState(Input.TYPE_ACTIVE)
            }

            R.id.btn_add_helper -> {
                addHelperView()
            }

            R.id.btn_set_error -> {
                setErrorText()
            }

            R.id.btn_clear_error -> {
                clearErrorText()
            }

            R.id.btn_replace_function -> {
//                replaceFunction()
            }

            R.id.btn_enable -> {
                binding.input.setState(Input.TYPE_STATIC)
                binding.inputField2.setState(Input.TYPE_STATIC)
            }

            R.id.btn_disable -> {
                binding.input.setState(Input.TYPE_DISABLE)
                binding.inputField2.setState(Input.TYPE_DISABLE)
            }

            R.id.btn_reset -> {
                binding.input.reset()
                binding.inputField2.reset()
            }
        }
    }

    private fun getView(parent: ViewGroup?, attachToParent: Boolean = false): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.layout_input_function, null, attachToParent)
    }

    private fun setLabelText() {
        binding.input.setLabelText("You clicked set a label text button.")
        binding.inputField2.setLabelText("You clicked set a label text button.")
    }

    private fun setInputFieldFunction() {
        binding.input.setContentBackgroundType(2)
        binding.input.getContentEditText()?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.input.getContentEditText()?.setText("123456789")
    }


    private fun addHelperView() {
        val helperView = LayoutInflater.from(context).inflate(
            R.layout.layout_input_helper, binding.input, false
        )
        binding.input.setSupportingView(helperView)
        val helperView2 = LayoutInflater.from(context).inflate(
            R.layout.layout_input_helper, binding.inputField2, false
        )
        binding.inputField2.setSupportingView(helperView2)
    }

    private fun clearErrorText() {
        binding.input.clearErrorText()
        binding.inputField2.clearErrorText()
    }

    private fun setErrorText() {
        binding.input.setErrorText("You clicked set error text button.")
        binding.inputField2.setErrorText("You clicked set error text button.")
    }
}