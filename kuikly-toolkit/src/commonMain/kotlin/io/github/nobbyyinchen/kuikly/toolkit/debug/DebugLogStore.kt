/*
 * MIT License
 * Copyright (c) 2026 KuiklyToolkit contributors
 */
package io.github.nobbyyinchen.kuikly.toolkit.debug

enum class DebugLogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR,
}

data class DebugLogEntry(
    val id: Int,
    val timeText: String,
    val pageName: String,
    val tag: String,
    val message: String,
    val level: DebugLogLevel,
    val repeatCount: Int = 1,
) {
    val searchText: String = "$pageName $tag $message".lowercase()

    val displayText: String
        get() = buildString {
            if (timeText.isNotBlank()) append("[$timeText]")
            if (pageName.isNotBlank()) append("[$pageName]")
            append("[${level.name}]")
            if (tag.isNotBlank()) append("[$tag]")
            if (repeatCount > 1) append("[x$repeatCount]")
            if (isNotEmpty()) append(' ')
            append(message)
        }
}

/**
 * Platform-independent bounded log store. Time and page information are supplied by the host so
 * the library does not depend on a clock, Pager subclass, storage module or bridge implementation.
 */
class DebugLogStore(
    maxLogSize: Int = DEFAULT_MAX_LOG_SIZE,
    private val pageNameProvider: () -> String = { "" },
    private val timeTextProvider: () -> String = { "" },
) {
    private var nextId = 1
    private var nextListenerId = 1
    private var capacity = maxLogSize.coerceAtLeast(1)
    private val entries = mutableListOf<DebugLogEntry>()
    private val listeners = mutableMapOf<Int, (List<DebugLogEntry>) -> Unit>()

    fun append(
        tag: String,
        message: String,
        level: DebugLogLevel = DebugLogLevel.INFO,
        pageName: String = pageNameProvider(),
        timeText: String = timeTextProvider(),
    ): DebugLogEntry {
        val last = entries.lastOrNull()
        val entry = if (last != null &&
            last.pageName == pageName &&
            last.tag == tag &&
            last.message == message &&
            last.level == level
        ) {
            last.copy(
                id = nextId++,
                timeText = timeText,
                repeatCount = last.repeatCount + 1,
            ).also { entries[entries.lastIndex] = it }
        } else {
            DebugLogEntry(
                id = nextId++,
                timeText = timeText,
                pageName = pageName,
                tag = tag,
                message = message,
                level = level,
            ).also(entries::add)
        }
        trimToCapacity()
        notifyChanged()
        return entry
    }

    fun snapshot(): List<DebugLogEntry> = entries.toList()

    fun search(
        keyword: String = "",
        pageName: String = "",
        tag: String = "",
        minimumLevel: DebugLogLevel = DebugLogLevel.DEBUG,
    ): List<DebugLogEntry> {
        val normalizedKeyword = keyword.trim().lowercase()
        return entries.filter { entry ->
            entry.level.ordinal >= minimumLevel.ordinal &&
                (pageName.isEmpty() || entry.pageName == pageName) &&
                (tag.isEmpty() || entry.tag == tag) &&
                (normalizedKeyword.isEmpty() || normalizedKeyword in entry.searchText)
        }
    }

    fun clear() {
        if (entries.isEmpty()) return
        entries.clear()
        notifyChanged()
    }

    fun setMaxLogSize(maxLogSize: Int) {
        capacity = maxLogSize.coerceAtLeast(1)
        val changed = trimToCapacity()
        if (changed) notifyChanged()
    }

    fun addListener(listener: (List<DebugLogEntry>) -> Unit): Int {
        val listenerId = nextListenerId++
        listeners[listenerId] = listener
        listener(snapshot())
        return listenerId
    }

    fun removeListener(listenerId: Int) {
        listeners.remove(listenerId)
    }

    private fun trimToCapacity(): Boolean {
        var changed = false
        while (entries.size > capacity) {
            entries.removeAt(0)
            changed = true
        }
        return changed
    }

    private fun notifyChanged() {
        val snapshot = snapshot()
        listeners.values.toList().forEach { it(snapshot) }
    }

    companion object {
        const val DEFAULT_MAX_LOG_SIZE = 500
    }
}

