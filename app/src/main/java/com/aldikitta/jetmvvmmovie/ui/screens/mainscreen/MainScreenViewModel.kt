package com.aldikitta.jetmvvmmovie.ui.screens.mainscreen

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldikitta.jetmvvmmovie.data.model.BaseModel
import com.aldikitta.jetmvvmmovie.data.model.Genres
import com.aldikitta.jetmvvmmovie.data.repository.MovieRepository
import com.aldikitta.jetmvvmmovie.utils.network.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(private val movieRepository: MovieRepository) :
    ViewModel() {
    val genres: MutableState<DataState<Genres>?> = mutableStateOf(null)
    val searchData: MutableState<DataState<BaseModel>?> = mutableStateOf(null)

    fun genreList() {
        viewModelScope.launch {
            movieRepository.genreList().onEach {
                genres.value = it
            }.launchIn(viewModelScope)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @FlowPreview
    fun searchApi(searchKey: String) {
        viewModelScope.launch {
            flowOf(searchKey).debounce(300)
                .filter {
                    it.trim().isEmpty().not()
                }
                .distinctUntilChanged()
                .flatMapLatest {
                    movieRepository.search(it)
                }.collect {
                    if (it is DataState.Success) {
                        it.data
                        Timber.e(" data ${it.data.totalPages}")
                    }
                    searchData.value = it
                }
        }
    }
}