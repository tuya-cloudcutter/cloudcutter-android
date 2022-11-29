/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.forEach
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.sizeDp
import dagger.hilt.android.AndroidEntryPoint
import io.github.cloudcutter.R
import io.github.cloudcutter.databinding.MainActivityBinding
import io.github.cloudcutter.ui.viewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), CoroutineScope {

	override val coroutineContext = Job() + Dispatchers.IO

	private val b by viewBinding(MainActivityBinding::inflate)
	private val vm by viewModels<MainViewModel>()
	private val navController by lazy { findNavController(R.id.nav_host_fragment) }
	private lateinit var appBarConfiguration: AppBarConfiguration

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(b.root)
		setSupportActionBar(b.toolbar)

		b.toolbar.setupWithNavController(navController)
		b.bottomNav.setupWithNavController(navController)
		appBarConfiguration = AppBarConfiguration(navController.graph)
		setupActionBarWithNavController(navController, appBarConfiguration)

		b.bottomNav.menu.forEach {
			val icon: IIcon = when (it.itemId) {
				R.id.menu_devices -> CommunityMaterial.Icon2.cmd_lightbulb_outline
				R.id.menu_profiles -> CommunityMaterial.Icon.cmd_code_json
				R.id.menu_dumps -> CommunityMaterial.Icon3.cmd_server
				else -> CommunityMaterial.Icon2.cmd_help
			}
			it.icon = IconicsDrawable(this, icon).apply {
				sizeDp = 24
			}
		}
	}

	override fun onSupportNavigateUp(): Boolean {
		return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
	}
}
