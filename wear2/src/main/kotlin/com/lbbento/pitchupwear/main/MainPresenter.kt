package com.lbbento.pitchupwear.main

import android.util.Log
import com.lbbento.pitchuptuner.GuitarTunerReactive
import com.lbbento.pitchuptuner.service.TunerResult
import com.lbbento.pitchuptuner.service.TuningStatus
import com.lbbento.pitchupwear.AppSchedulers
import com.lbbento.pitchupwear.di.ForActivity
import com.lbbento.pitchupwear.ui.BasePresenter
import com.lbbento.pitchupwear.util.PermissionHandler
import rx.Subscription
import javax.inject.Inject

@ForActivity
class MainPresenter @Inject constructor(val appSchedulers: AppSchedulers, val permissionHandler: PermissionHandler, val guitarTunerReactive: GuitarTunerReactive, val mapper: TunerServiceMapper) : BasePresenter<MainView>() {

    private var tunerServiceSubscription: Subscription? = null

    override fun onCreated() {
        super.onCreated()
        mView.setAmbientEnabled()
        mView.setupGauge()
    }

    override fun onStop() {
        tunerServiceSubscription!!.unsubscribe()
    }

    override fun onViewResuming() {
        if (permissionHandler.handleMicrophonePermission()) {
            tunerServiceSubscription = guitarTunerReactive.listenToNotes()
                    .subscribeOn(appSchedulers.io())
                    .observeOn(appSchedulers.ui())
                    .subscribe(
                            { tunerResult: TunerResult? -> tunerResultReceived(mapper.tunerResultToViewModel(tunerResult!!)) },
                            { e: Throwable -> tunerResultError(e) })
        }
    }

    private fun tunerResultReceived(tunerViewModel: TunerViewModel) {
        if (tunerViewModel.tunningStatus != TuningStatus.DEFAULT) {
            if (mView.getCurrentNote() != tunerViewModel.note) {
                val maxFreq = (tunerViewModel.expectedFrequency + 3f).toInt()
                val minFreq = (tunerViewModel.expectedFrequency - 3f).toInt()
                mView.updateMaxMinFreq(minFreq, maxFreq)
                mView.updateToNote(tunerViewModel.note)
            }
            mView.updateFrequency((tunerViewModel.expectedFrequency + tunerViewModel.diffFrequency).toFloat())
        } else {
            mView.updateToDefaultStatus()
        }
    }

    private fun tunerResultError(e: Throwable) {
        mView.informError()
        Log.e("Lucas", "Error tuning: " + e.stackTrace.toString())
    }
}