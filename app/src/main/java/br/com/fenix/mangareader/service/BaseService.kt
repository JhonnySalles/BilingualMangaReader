package br.com.fenix.mangareader.service

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import br.com.fenix.mangareader.repository.DBRepository

abstract class BaseService<T>(application: Application) : AndroidViewModel(application) {
    private val mContext = application.applicationContext
    protected val mDBRepository: DBRepository = DBRepository.getInstance(mContext)

    abstract fun save(obj: T)
    abstract fun update(obj: T)
    abstract fun delete(obj: T)
    abstract fun list(): List<T>?
    abstract fun get(id: Long): T?

}