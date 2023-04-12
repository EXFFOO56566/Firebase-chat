package com.devlomi.fireapp.activities.main

import android.arch.lifecycle.MutableLiveData
import com.devlomi.fireapp.common.ScopedViewModel
import com.devlomi.fireapp.common.extensions.toDeffered
import com.devlomi.fireapp.job.DeleteStatusJob
import com.devlomi.fireapp.model.TextStatus
import com.devlomi.fireapp.model.constants.StatusType
import com.devlomi.fireapp.model.realms.Status
import com.devlomi.fireapp.model.realms.User
import com.devlomi.fireapp.utils.FireConstants
import com.devlomi.fireapp.utils.RealmHelper
import com.devlomi.fireapp.utils.TimeHelper
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainViewModel(uiContext: CoroutineContext) : ScopedViewModel(uiContext) {

    private val realmHelper = RealmHelper.getInstance()
    val statusLiveData = MutableLiveData<Unit>()
    var lastSyncTime = 0L

    companion object {
        //15Sec
        const val WAIT_TIME = 15000
    }

    fun fetchStatuses(users: List<User>) {

        //wait for 15 sec before re-fetching statuses
        if (lastSyncTime == 0L || System.currentTimeMillis() - lastSyncTime > WAIT_TIME) {
            launch {
                try {
                    val statusesIds = mutableListOf<String>()
                    fetchImageAndVideosStatuses(users, statusesIds)
                    fetchTextStatuses(users, statusesIds)
                    realmHelper.deleteDeletedStatusesLocally(statusesIds)
                    lastSyncTime = System.currentTimeMillis()
                    updateUi()

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun updateUi() {
        statusLiveData.value = Unit
    }


    private fun handleStatus(dataSnapshot: DataSnapshot, statusesIds: MutableList<String>) {


        if (dataSnapshot.value != null) {
            //get every status
            for (snapshot in dataSnapshot.children) {
                val userId = snapshot.ref.parent!!.key
                val statusId = snapshot.key
                val status = snapshot.getValue(Status::class.java)
                status!!.statusId = statusId
                status.userId = userId

                if (status.type == StatusType.TEXT) {
                    val textStatus = snapshot.getValue(TextStatus::class.java)
                    textStatus!!.statusId = statusId!!
                    status.textStatus = textStatus
                }

                statusesIds.add(statusId!!)
                //check if status is exists in local database , if not save it
                if (realmHelper.getStatus(status.statusId) == null) {
                    realmHelper.saveStatus(userId, status)
                    //schedule a job after 24 hours to delete this status locally
                    DeleteStatusJob.schedule(userId, statusId)
                }


            }

        }
    }

    private suspend fun fetchImageAndVideosStatuses(users: List<User>, statusesIds: MutableList<String>) {
        //add all statuses to this list to delete deleted statuses if needed
        //get current time before 24 hours (Yesterday)
        val timeBefore24Hours = TimeHelper.getTimeBefore24Hours()
        //get all user statuses that are not passed 24 hours


        val jobs = mutableListOf<Deferred<DataSnapshot>>()


        val job = async {
            for (user in users!!) {
                val query = FireConstants.statusRef.child(user.uid)
                        .orderByChild("timestamp")
                        .startAt(timeBefore24Hours.toDouble())


                val dataSnapshot = query.toDeffered()

                jobs.add(dataSnapshot)
            }

        }


        job.await()
        val datasnapshots = jobs.awaitAll()
        datasnapshots.forEach {
            handleStatus(it, statusesIds)
        }


    }

    private suspend fun fetchTextStatuses(users: List<User>, statusesIds: MutableList<String>) {
//        val statusesIds = mutableListOf<String>()

        //add all statuses to this list to delete deleted statuses if needed
        //get current time before 24 hours (Yesterday)
        val timeBefore24Hours = TimeHelper.getTimeBefore24Hours()
        //get all user statuses that are not passed 24 hours


        val jobs = mutableListOf<Deferred<DataSnapshot>>()

        val job = async {
            for (user in users!!) {
                val query = FireConstants.textStatusRef.child(user.uid)
                        .orderByChild("timestamp")
                        .startAt(timeBefore24Hours.toDouble())


                val dataSnapshot = query.toDeffered()

                jobs.add(dataSnapshot)
            }

        }


        job.await()
        val datasnapshots = jobs.awaitAll()
        datasnapshots.forEach {
            handleStatus(it, statusesIds)
        }


    }


}


