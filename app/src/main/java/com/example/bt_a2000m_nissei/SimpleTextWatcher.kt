package com.example.bt_a2000m_nissei

import android.text.Editable
import android.text.TextWatcher

class SimpleTextWatcher(private val onChanged: (Editable?) -> Unit) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable?) { onChanged(s) }
}
