package com.example.uts_map

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navController = findNavController(R.id.nav_host_fragment)

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.popBackStack(R.id.navigation_home, false)
                    navController.navigate(R.id.navigation_home)
                    true
                }
                R.id.navigation_calendar -> {
                    navController.popBackStack(R.id.navigation_calendar, false)
                    navController.navigate(R.id.navigation_calendar)
                    true
                }
                R.id.navigation_map -> {
                    navController.popBackStack(R.id.navigation_map, false)
                    navController.navigate(R.id.navigation_map)
                    true
                }
                R.id.navigation_profile -> {
                    navController.popBackStack(R.id.navigation_profile, false)
                    navController.navigate(R.id.navigation_profile)
                    true
                }
                else -> false
            }
        }
        // Akses FloatingActionButton
        val fabAddNew: FloatingActionButton = findViewById(R.id.fab_addnew)

        // Mengatur listener untuk navigasi
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.navigation_addnew) {
                fabAddNew.visibility = View.VISIBLE // Tampilkan kembali tombol "Add"
            } else {
                fabAddNew.visibility = View.GONE // Sembunyikan saat di NewNotesFragment
            }
        }

        // Listener untuk klik tombol "Add New"
        fabAddNew.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(R.id.nav_host_fragment, true) // Bersihkan tumpukan sampai root
                .build()

            // Navigasi ke NewNotesFragment dengan membersihkan tumpukan
            navController.navigate(R.id.navigation_addnew, null, navOptions)

            // Sembunyikan tombol "Add New" setelah ditekan
            fabAddNew.visibility = View.GONE
        }

        val drawerNavView: NavigationView = findViewById(R.id.nav_view)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_home), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        drawerNavView.setupWithNavController(navController)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}