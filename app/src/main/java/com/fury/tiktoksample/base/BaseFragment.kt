package com.fury.tiktoksample.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import kotlin.coroutines.CoroutineContext

/***
 * Created By Amir Fury on November 10 2021
 *
 * Email: Fury.amir93@gmail.com
 * */

abstract class BaseFragment<B : ViewDataBinding> : Fragment(), CoroutineScope, KodeinAware {

    private lateinit var job: Job

    override val kodein: Kodein by kodein()


    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private lateinit var fragmentBinding: B
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    @get:LayoutRes
    protected abstract val layoutResId: Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: ViewDataBinding =
            DataBindingUtil.inflate(inflater, layoutResId, container, false)
        onFragmentReady(savedInstanceState, rootView as B)
        fragmentBinding = rootView
        return rootView.root
    }

    protected abstract fun onFragmentReady(instanceState: Bundle?, binding: B)

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    protected val binding: B by lazy { fragmentBinding }

}