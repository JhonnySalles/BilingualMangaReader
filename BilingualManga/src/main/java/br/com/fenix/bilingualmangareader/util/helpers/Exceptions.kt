package br.com.fenix.bilingualmangareader.util.helpers

class BackupError(message: String) : Exception(message)
class InvalidDbFile(message: String) : Exception(message)
class InvalidDatabase(message: String) : Exception(message)
class RestoredNewDatabase(message: String) : Exception(message)
class ErrorRestoreDatabase(message: String) : Exception(message)