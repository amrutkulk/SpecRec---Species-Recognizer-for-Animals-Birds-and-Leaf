package com.example.species.data.remote

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.MutableState
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translator


class GoogleTranslate {
    public fun downloadModal(
        input: String,
        context: Context,
        translator: Translator,
        translatedTitle: MutableState<String>
    ) {
        // below line is use to download the modal which
        // we will require to translate in german language
        var conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        // below line is use to download our modal.
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener(OnSuccessListener<Void?> { // this method is called when modal is downloaded successfully.
                Toast.makeText(
                    context,
                    "Please wait language modal is being downloaded.",
                    Toast.LENGTH_SHORT
                ).show()

                // calling method to translate our entered text.
                translateLanguage(input, context, translator, translatedTitle)
            }).addOnFailureListener(OnFailureListener {
                Toast.makeText(
                    context,
                    "Fail to download modal",
                    Toast.LENGTH_SHORT
                ).show()
            })
    }

    private fun translateLanguage(
        input: String,
        context: Context,
        translator: Translator,
        translatedTitle: MutableState<String>
    ) {
        translator.translate(input)
            .addOnSuccessListener(OnSuccessListener<String?> { translatedText ->
                Toast.makeText(context, translatedText.toString(), Toast.LENGTH_SHORT).show()
                translatedTitle.value = translatedText.toString()
            })
            .addOnFailureListener(
                OnFailureListener {
                    Toast.makeText(
                        context,
                        "Fail to translate",
                        Toast.LENGTH_SHORT
                    ).show()
                })
    }
}