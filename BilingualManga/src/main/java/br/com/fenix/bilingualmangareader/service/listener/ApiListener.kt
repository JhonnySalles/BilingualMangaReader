package br.com.fenix.bilingualmangareader.service.listener

interface ApiListener<T> {
    fun onSuccess(result: T)
    fun onFailure(message: String)
}