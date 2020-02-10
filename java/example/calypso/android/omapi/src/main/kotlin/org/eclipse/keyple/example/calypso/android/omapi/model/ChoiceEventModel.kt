package org.eclipse.keyple.example.calypso.android.omapi.model

data class ChoiceEventModel(val title: String, val choices: List<String> = arrayListOf(), val callback: (choice: String) -> Unit)
    : EventModel(TYPE_MULTICHOICE, title)