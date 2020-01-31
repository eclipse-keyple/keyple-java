package org.eclipse.keyple.example.calypso.android.omapi.model

data class EventModel(val type: Int, val text:String){
    companion object{
        const val TYPE_HEADER = 0
        const val TYPE_ACTION = 1
        const val TYPE_RESULT = 2
    }
}