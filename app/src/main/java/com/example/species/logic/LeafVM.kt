package com.example.species.logic

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.species.data.modals.leaf.Data
import com.example.species.data.remote.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LeafVM(job: Job) : ViewModel() {

    val myJob = job

    val leafData = MutableLiveData(mutableStateOf<Data?>(null))

    fun getLeafData(context: Context, repo: MainRepository, job: Job, query: String) {
        viewModelScope.launch(job + Dispatchers.Main) {
            viewModelScope.launch(Dispatchers.IO) {
                repo.LeafInfo(context, query, leafData.value)
                println("VM:" + leafData.value?.value?.title)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        myJob.cancel()
    }

}