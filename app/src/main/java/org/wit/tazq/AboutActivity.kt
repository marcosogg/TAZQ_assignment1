// AboutActivity.kt
package org.wit.tazq

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.wit.tazq.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "About"

        // Set about information
        binding.textViewAbout.text = "TAZQ App\nVersion 1.0\nDeveloped by Marcos Gomes."
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
