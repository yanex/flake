package org.yanex.flaketest

import android.os.Bundle
import org.yanex.flake.FlakeActivity
import org.yanex.flake.FlakeLayout
import org.yanex.flake.FlakeManager
import org.yanex.flaketest.flakes.ListFlake

class MainActivity : FlakeActivity() {

    private val topLevelFlakeManager by lazy {
        val flakeLayout = findViewById(R.id.flakeLayout) as FlakeLayout
        FlakeManager.create(flakeLayout, flakeContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val flakeManager = topLevelFlakeManager
        flakeManager.retainPreviousFlake = true

        if (!flakeManager.restoreState()) {
            flakeManager.show(ListFlake())
        }
    }

    override fun onBackPressed() {
        if (!topLevelFlakeManager.onBackPressed()) {
            super.onBackPressed()
        }
    }
}
