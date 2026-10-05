package ru.itmo.contacts

import android.content.ContentResolver
import android.provider.ContactsContract.CommonDataKinds.Phone

data class Contact(val id: Long, val name: String, val phoneNumber: String) {
    val initial: Char
        get() = name.firstOrNull()?.takeIf(Char::isLetter)?.uppercaseChar() ?: NO_LETTER_INITIAL
}

data class ContactSection(val initial: Char, val contacts: List<Contact>)

const val NO_LETTER_INITIAL = '&'

private val PROJECTION = arrayOf(Phone._ID, Phone.DISPLAY_NAME, Phone.NUMBER)

fun ContentResolver.fetchContacts(): List<Contact> =
    query(Phone.CONTENT_URI, PROJECTION, null, null, Phone.SORT_KEY_PRIMARY)?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(Phone._ID)
        val nameColumn = cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME)
        val numberColumn = cursor.getColumnIndexOrThrow(Phone.NUMBER)
        buildList {
            while (cursor.moveToNext()) {
                add(
                    Contact(
                        id = cursor.getLong(idColumn),
                        name = cursor.getString(nameColumn).orEmpty(),
                        phoneNumber = cursor.getString(numberColumn).orEmpty(),
                    )
                )
            }
        }
    }.orEmpty()

fun List<Contact>.groupByInitial(): List<ContactSection> =
    groupBy(Contact::initial).map { (initial, contacts) -> ContactSection(initial, contacts) }
