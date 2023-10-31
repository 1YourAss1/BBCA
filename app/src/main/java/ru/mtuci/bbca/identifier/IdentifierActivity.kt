package ru.mtuci.bbca.identifier

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import ru.mtuci.bbca.R
import ru.mtuci.bbca.main.MainActivity

class IdentifierActivity : AppCompatActivity() {
    private val viewModel: IdentifierViewModel by viewModels()

    private val identifierEditText: TextInputEditText by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.identifierEditText)
    }

    private val repeatedIdentifierEditText: TextInputEditText by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.identifierRepeatedEditText)
    }

    private val okButton: Button by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.okButton)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_identifier)

        identifierEditText.addTextChangedListener { text ->
            viewModel.onIdentifierChange(text?.toString() ?: "")
        }

        repeatedIdentifierEditText.addTextChangedListener { text ->
            viewModel.onRepeatedIdentifierChange(text?.toString() ?: "")
        }

        okButton.setOnClickListener {
            viewModel.identify()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.identifierError.collect { errorResId ->
                        identifierEditText.error = errorResId?.let { getString(it) }
                    }
                }

                launch {
                    viewModel.repeatedIdentifierError.collect { errorResId ->
                        repeatedIdentifierEditText.error = errorResId?.let { getString(it) }
                    }
                }

                launch {
                    viewModel.doneSideEffect.collect {
                        startActivity(
                            Intent(this@IdentifierActivity, MainActivity::class.java).apply {
                                putExtra("identifier", viewModel.identifier)
                            }
                        )

                        finish()
                    }
                }
            }
        }
    }
}