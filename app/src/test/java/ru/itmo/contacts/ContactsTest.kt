package ru.itmo.contacts

import org.junit.Assert.assertEquals
import org.junit.Test

class ContactsTest {

    @Test
    fun initialIsUppercaseFirstLetter() {
        assertEquals('А', Contact(1, "аня", "+7").initial)
        assertEquals('B', Contact(2, "Bob", "+7").initial)
    }

    @Test
    fun initialOfNameWithoutLeadingLetterIsPlaceholder() {
        assertEquals(NO_LETTER_INITIAL, Contact(1, "", "+7").initial)
        assertEquals(NO_LETTER_INITIAL, Contact(2, "+7 921", "+7 921").initial)
    }

    @Test
    fun groupsContactsByInitialKeepingOrder() {
        val ana = Contact(1, "Ана", "1")
        val anton = Contact(2, "антон", "2")
        val boris = Contact(3, "Борис", "3")

        val sections = listOf(ana, anton, boris).groupByInitial()

        assertEquals(
            listOf(ContactSection('А', listOf(ana, anton)), ContactSection('Б', listOf(boris))),
            sections,
        )
    }

    @Test
    fun emptyListHasNoSections() {
        assertEquals(emptyList<ContactSection>(), emptyList<Contact>().groupByInitial())
    }
}
