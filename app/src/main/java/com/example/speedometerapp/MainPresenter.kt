package com.example.speedometerapp

class MainPresenter(private val view: UpdateView): PresenterType {

    private var speedCalculator: SpeedCalculator? = null

    override fun calculateSpeed() {
        if (speedCalculator == null) {
            speedCalculator = SpeedCalculator(view.getContext())
        }
        speedCalculator?.doCalculations(view::updateCurrentSpeedView,
                                       view::updateFrom10To30SpeedView,
                                       view::updateFrom30To10SpeedView)
    }
}

interface PresenterType {
    fun calculateSpeed()
}