package com.lbbento.pitchupwear.main

import android.util.Log
import com.lbbento.pitchuptuner.service.TunerResult
import com.lbbento.pitchuptuner.service.TunerService
import com.lbbento.pitchupwear.AppSchedulers
import com.lbbento.pitchupwear.di.ForActivity
import com.lbbento.pitchupwear.ui.BasePresenter
import com.lbbento.pitchupwear.util.PermissionHandler
import rx.Subscription
import javax.inject.Inject

@ForActivity
class MainPresenter @Inject constructor(val appSchedulers: AppSchedulers, val permissionHandler: PermissionHandler, val tunerService: TunerService, val mapper: TunerServiceMapper) : BasePresenter<MainView>() {

    private var tunerServiceSubscription: Subscription? = null

    override fun onCreated() {
        super.onCreated()
        mView.setAmbientEnabled()
    }

    override fun onStop() {
        tunerServiceSubscription!!.unsubscribe()
    }

    override fun onViewResuming() {
        if (permissionHandler.handleMicrophonePermission()) {
            tunerServiceSubscription = tunerService.getNotes()
                    .subscribeOn(appSchedulers.io())
                    .observeOn(appSchedulers.ui())
                    .subscribe(
                            { tunerResult: TunerResult? -> tunerResultReceived(mapper.tunerResultToViewModel(tunerResult!!)) },
                            { _: Throwable -> tunerResultError() })
        }
    }

    private fun tunerResultReceived(tunerViewModel: TunerViewModel) {
        mView.updateTunerView(tunerViewModel = tunerViewModel)
    }

    private fun tunerResultError() {
        Log.e("Lucas", "Error tuning") //TODO
    }
}