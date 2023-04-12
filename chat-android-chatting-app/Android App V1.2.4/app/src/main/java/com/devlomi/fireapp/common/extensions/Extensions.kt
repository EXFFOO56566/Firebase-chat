package com.devlomi.fireapp.common.extensions

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

fun Location.toLatLng(): LatLng = LatLng(this.latitude, this.longitude)
fun LatLng.toLatLngString(): String = "${this.latitude},${this.longitude}"

fun DatabaseReference.toDeffered(): Deferred<DataSnapshot> {
    val deferred = CompletableDeferred<DataSnapshot>()

    deferred.invokeOnCompletion {
        if (deferred.isCancelled) {
            // optional, handle coroutine cancellation however you'd like here
        }
    }

    this.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            deferred.completeExceptionally(p0.toException())
        }

        override fun onDataChange(p0: DataSnapshot) {
            deferred.complete(p0)
        }
    })
    return deferred
}

fun Query.toDeffered(): Deferred<DataSnapshot> {
    val deferred = CompletableDeferred<DataSnapshot>()

    deferred.invokeOnCompletion {
        if (deferred.isCancelled) {
            // optional, handle coroutine cancellation however you'd like here
        }
    }

    this.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            deferred.completeExceptionally(p0.toException())
        }

        override fun onDataChange(p0: DataSnapshot) {
            deferred.complete(p0)
        }
    })
    return deferred
}

fun Task<Void>.toDeffered(): Deferred<Boolean> {

    val deferred = CompletableDeferred<Boolean>()

    deferred.invokeOnCompletion {
        if (deferred.isCancelled) {
            // optional, handle coroutine cancellation however you'd like here
        }
    }

    this.addOnCompleteListener {
        deferred.complete(it.isSuccessful)
    }



    return deferred
}

fun StorageTask<FileDownloadTask.TaskSnapshot>.toDeffered(): Deferred<Boolean> {

    val deferred = CompletableDeferred<Boolean>()

    deferred.invokeOnCompletion {
        if (deferred.isCancelled) {
            // optional, handle coroutine cancellation however you'd like here
        }
    }

    this.addOnCompleteListener {
        deferred.complete(it.isSuccessful)
    }



    return deferred
}

fun StorageTask<UploadTask.TaskSnapshot>.toDefferedWithTask(): CompletableDeferred<Task<UploadTask.TaskSnapshot>> {

    val deferred = CompletableDeferred<Task<UploadTask.TaskSnapshot>>()

    deferred.invokeOnCompletion {
        if (deferred.isCancelled) {
            // optional, handle coroutine cancellation however you'd like here
        }
    }

    this.addOnCompleteListener {
        deferred.complete(it)
    }



    return deferred
}

