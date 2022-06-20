package com.foobar.signage

import android.R
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.TableRow
import android.widget.TextView

class DialogBox {

    var gridLayout: GridLayout? = null
    lateinit var alertDialog: AlertDialog
    var message: String? = null
    var timer: CountDownTimer? = null

    fun dialogBox(msg: String, countDownSec: Int, activity: Context) {
        makeDialog(msg, countDownSec, activity)
/*        if (alertDialog == null) {
            makeDialog(msg, countDownSec, activity)
        } else if (msg != message) {
            timer!!.cancel()
            alertDialog!!.cancel()
            makeDialog(msg, countDownSec, activity)
        } else {
            // make a new timer to ensure that the last person that tries to connect sees the screen key dialog for at
            // least the countDownSec seconds
            timer!!.cancel()
            makeTimer(countDownSec)
        }*/
    }

    private fun makeDialog(msg: String, countDownSec: Int, activity: Context) {
        setupLayout(activity, msg)
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setView(gridLayout)
        alertDialog = alertDialogBuilder.create()
        alertDialog.window?.setBackgroundDrawableResource(R.color.background_light)
        alertDialog.show()
        message = msg
        makeTimer(countDownSec)
    }

    private fun makeTimer(countDownSec: Int) {
        timer = object : CountDownTimer((countDownSec * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                alertDialog.cancel()
            }
        }.start()
    }

    fun cancel() {
        if (alertDialog != null) {
            alertDialog!!.cancel()
        }
        if (timer != null) {
            timer!!.cancel()
        }
    }

    private fun setupLayout(activity: Context, msg: String) {
        gridLayout = GridLayout(activity)
        val layoutParams = GridLayout.LayoutParams()
        layoutParams.width = GridLayout.LayoutParams.WRAP_CONTENT
        layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT
        layoutParams.setGravity(Gravity.LEFT)
        gridLayout!!.orientation = GridLayout.VERTICAL
        gridLayout!!.rowCount = msg.length
        gridLayout!!.columnCount = 1
        gridLayout!!.layoutParams = layoutParams
        for (i in 0 until msg.length) {
            val tv = createTextView(activity)
            tv.text = msg.subSequence(i, i + 1)
            gridLayout!!.addView(tv, i)
        }
    }

    private fun createTextView(activity: Context): TextView {
        val textViewParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f
        )
        textViewParams.setMargins(15, 15, 15, 15)
        val keyText = TextView(activity)
        keyText.layoutParams = textViewParams
        keyText.textSize = 154f
        keyText.setTextColor(Color.WHITE)
        keyText.gravity = Gravity.CENTER
        keyText.setTypeface(null, Typeface.BOLD)
        customTextView(keyText)
        return keyText
    }

    private fun customTextView(v: View) {
        val shape = GradientDrawable()
        val colors = intArrayOf(-0x5f000000, -0x5f000000)
        shape.shape = GradientDrawable.RECTANGLE
        shape.orientation = GradientDrawable.Orientation.LEFT_RIGHT
        shape.colors = colors
        shape.setBounds(0, 0, 50, 50)
        shape.cornerRadii = floatArrayOf(25f, 25f, 25f, 25f, 25f, 25f, 25f, 25f)
        shape.setSize(142, 154)
        v.background = shape
    }
}