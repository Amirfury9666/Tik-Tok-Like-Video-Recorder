package com.fury.tiktoksample.base

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import kotlin.coroutines.CoroutineContext

/***
 * Created By Amir Fury on November 10 2021
 *
 * Email: Fury.amir93@gmail.com
 * */

abstract class BaseActivity<B : ViewDataBinding> : AppCompatActivity(), CoroutineScope,
    KodeinAware {

    override val kodein: Kodein by kodein()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private lateinit var job: Job

    private lateinit var activityBinding: B


    override fun onCreate(savedInstanceState: Bundle?) {
        job = Job()
        onPreCreate()
        super.onCreate(savedInstanceState)
        val binding: ViewDataBinding = DataBindingUtil.setContentView(this, layoutRes)
        setSupportActionBar(getToolbar(binding as B))
        activityBinding = binding
        onActivityReady(savedInstanceState, binding)

    }

    @get:LayoutRes
    protected abstract val layoutRes: Int
    open fun onPreCreate() {}
    protected abstract fun getToolbar(binding: B): Toolbar?
    protected abstract fun onActivityReady(instanceState: Bundle?, binding: B)


    open fun addTabLayoutMediator(
        tabLayout: TabLayout,
        viewPager2: ViewPager2,
        tabTitles: ArrayList<String>?,
        tabIcons: ArrayList<Drawable>?
    ) {
        TabLayoutMediator(tabLayout, viewPager2) { tab: TabLayout.Tab, _position: Int ->
            tabTitles?.let {
                for (i in it.indices) {
                    tab.text = it[_position]
                }
            }

            tabIcons?.let {
                for (i in it.indices) {
                    tab.icon = it[_position]
                }
            }
        }.attach()
    }

    fun enableBack() {
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
    }

    fun setToolbarTitle(title: String?) {
        supportActionBar?.let {
            it.title = title
        }
    }

    fun setToolbarTitleDisable() {
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    protected val binding: B by lazy { activityBinding }

}

