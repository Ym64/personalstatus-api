package me.m64diamondstar.status

class StatusManager {

    private var status: Int = 0

    fun setStatus(status: Int) {
        this.status = status
    }

    fun getStatus(): Int = status

}