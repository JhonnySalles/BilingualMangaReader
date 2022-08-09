package br.com.fenix.bilingualmangareader.view.ui.vocabulary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.model.entity.Vocabulary
import br.com.fenix.bilingualmangareader.service.repository.VocabularyRepository
import org.slf4j.LoggerFactory


class VocabularyViewModel(application: Application) : AndroidViewModel(application) {

    private val mLOGGER = LoggerFactory.getLogger(VocabularyViewModel::class.java)
    private val mVocabularyRepository: VocabularyRepository = VocabularyRepository(application.applicationContext)

    private var mVocabulary = MutableLiveData<MutableList<Vocabulary>>()
    val vocabulary: LiveData<MutableList<Vocabulary>> = mVocabulary


}