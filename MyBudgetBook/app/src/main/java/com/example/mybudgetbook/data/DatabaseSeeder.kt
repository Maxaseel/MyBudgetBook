package com.example.mybudgetbook.data

import com.example.mybudgetbook.data.entity.Gruppe
import com.example.mybudgetbook.data.entity.Kategorie
import com.example.mybudgetbook.data.entity.Konto
import com.example.mybudgetbook.data.entity.Person
import com.example.mybudgetbook.data.entity.Zahlungsart

class DatabaseSeeder(private val db: AppDatabase) {
    suspend fun seedIfEmpty() {
        if (db.kontoDao().count() > 0) return

        db.kontoDao().insert(Konto(name = "نقدي", startsaldo = 0.0))
        db.kontoDao().insert(Konto(name = "بنك", startsaldo = 0.0))

        db.kategorieDao().insert(Kategorie(name = "طعام"))
        db.kategorieDao().insert(Kategorie(name = "مواصلات"))
        db.kategorieDao().insert(Kategorie(name = "فواتير"))
        db.kategorieDao().insert(Kategorie(name = "ترفيه"))
        db.kategorieDao().insert(Kategorie(name = "راتب"))

        db.zahlungsartDao().insert(Zahlungsart(name = "نقدي"))
        db.zahlungsartDao().insert(Zahlungsart(name = "بطاقة"))

        db.personDao().insert(Person(name = "أنا"))
        db.gruppeDao().insert(Gruppe(name = "عام"))
    }
}
