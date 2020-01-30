package org.eclipse.keyple.example.calypso.android.omapi.activity

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.eclipse.keyple.core.seproxy.SeProxyService
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstantiationException
import org.eclipse.keyple.example.calypso.android.omapi.R
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPluginFactory

abstract class BasicActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* register Omapi Plugin to the SeProxyService */
        try {
            SeProxyService.getInstance().registerPlugin(AndroidOmapiPluginFactory(this))
        } catch (e: KeyplePluginInstantiationException) {
            e.printStackTrace()
        }
    }
    fun initActionBar(toolbar: Toolbar, title: String, subtitle: String){
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = title
        actionBar?.subtitle = subtitle
        actionBar?.setDisplayShowHomeEnabled(true)
        actionBar?.setLogo(R.mipmap.ic_launcher)
        actionBar?.setDisplayUseLogoEnabled(true)
    }

    fun showAlertDialog(t: Throwable){
        val builder = AlertDialog.Builder(this@BasicActivity)
        builder.setTitle(R.string.alert_dialog_title)
        builder.setMessage(getString(R.string.alert_dialog_message, t.message))
        val dialog = builder.create()
        dialog.show()

    }
}