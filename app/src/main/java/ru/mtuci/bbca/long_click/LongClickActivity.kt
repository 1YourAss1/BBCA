package ru.mtuci.bbca.long_click

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import ru.mtuci.bbca.Preferences
import ru.mtuci.bbca.R
import ru.mtuci.bbca.app_logger.CrashLogger
import ru.mtuci.bbca.main.MainActivity
import ru.mtuci.bbca.sensors_data_writer.sensorsDataWriter
import ru.mtuci.bbca.sensors_data_writer.userActivityDataWriter

class LongClickActivity : AppCompatActivity() {
    private val viewModel: LongClickViewModel by viewModels()

    private val adapter by lazy(LazyThreadSafetyMode.NONE) {
        Adapter(
            onItemLongClick = viewModel::onItemLongClick
        )
    }

    private val recyclerView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<RecyclerView>(R.id.elements)
    }

    private val progressView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<TextView>(R.id.progress)
    }

    private val userActivityDataWriter by lazy(LazyThreadSafetyMode.NONE) {
        userActivityDataWriter(
            currentSessionPath = Preferences.getSessionPath(),
            directoryName = "long_clicks",
            activityName = "long_clicks",
            activityColumns = listOf("timestamp", "orientation", "x_coordinate", "y_coordinate", "pressure", "action_type")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler(CrashLogger(this))

        setContentView(R.layout.activity_long_click)

        sensorsDataWriter(
            currentSessionPath = Preferences.getSessionPath(),
            directoryName = "long_clicks",
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.items.collect { items ->
                        adapter.submitList(items)
                    }
                }

                launch {
                    viewModel.taskDoneSideEffect.collect {
                        Toast.makeText(this@LongClickActivity, R.string.task_successfully_done, Toast.LENGTH_SHORT)
                            .show()

                        sendBroadcast(
                            Intent(MainActivity.TASK_DONE_KEY).apply {
                                putExtra(MainActivity.TASK_DONE_KEY, LONG_CLICKS_TASK)
                            }
                        )
                    }
                }

                launch {
                    viewModel.progress.collect { progress ->
                        progressView.text = getString(R.string.long_click_task, progress)
                    }
                }
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                userActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, event.x, event.y, event.pressure, event.action)
                )
            }
            MotionEvent.ACTION_MOVE -> {
                userActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, event.x, event.y, event.pressure, event.action)
                )
            }
            MotionEvent.ACTION_UP -> {
                userActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, event.x, event.y, event.pressure, event.action)
                )
            }
        }

        return super.dispatchTouchEvent(event)
    }

    private class Adapter(
        private val onItemLongClick: (AdapterItem) -> Unit
    ) : ListAdapter<AdapterItem, AdapterViewHolder>(AdapterDiffUtil()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder {
            val button = Button(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            return AdapterViewHolder(button, onItemLongClick)
        }

        override fun onBindViewHolder(holder: AdapterViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    private class AdapterViewHolder(
        itemView: View,
        onItemLongClick: (AdapterItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val button by lazy(LazyThreadSafetyMode.NONE) {
            itemView as? Button
        }

        private var item: AdapterItem? = null

        init {
            button?.setOnLongClickListener {
                val currentItem = item

                if (currentItem != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                    onItemLongClick(currentItem)
                    true
                } else {
                    false
                }
            }
        }

        fun bind(item: AdapterItem) {
            this.item = item
            button?.isInvisible = item.isDeleted
            button?.text = itemView.resources.getString(R.string.should_delete, item.id)
        }
    }

    private class AdapterDiffUtil : DiffUtil.ItemCallback<AdapterItem>() {
        override fun areItemsTheSame(oldItem: AdapterItem, newItem: AdapterItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AdapterItem, newItem: AdapterItem): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        const val LONG_CLICKS_TASK = "LONG_CLICKS_TASK"
    }
}