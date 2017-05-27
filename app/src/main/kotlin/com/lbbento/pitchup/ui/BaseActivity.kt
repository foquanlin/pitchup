package com.lbbento.pitchup.ui

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.wearable.activity.WearableActivity
import com.lbbento.pitchup.MainApplication
import com.lbbento.pitchup.di.ActivityComponent
import com.lbbento.pitchup.di.ActivityModule
import javax.inject.Inject

abstract class BaseActivity<P : BasePresenterView> : WearableActivity(), BaseView {

    @Inject
    lateinit var presenter: P

    val activityComponent: ActivityComponent
        get() {
            return (application as MainApplication).component.plus(ActivityModule(this))
        }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupInjection()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        presenter.onAttachedToWindow(this)
    }

    abstract fun setupInjection()
}