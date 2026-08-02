package com.sparkgym.domain.model

/**
 * Categorises an individual set. Stored in [SetLogEntity.setType].
 *
 * - **NORMAL** – a working set counted toward effective volume.
 * - **WARMUP** – lighter prep set, excluded from volume PRs.
 * - **DROP_SET** – reduced-weight continuation after the previous set's failure.
 * - **FAILURE** – taken to muscular failure, tracked for intensity programming.
 */
enum class SetType(val displayKey: String) {
    NORMAL("N"),
    WARMUP("W"),
    DROP_SET("D"),
    FAILURE("F");

    companion object {
        fun fromName(name: String): SetType =
            entries.firstOrNull { it.name == name } ?: NORMAL
    }
}
