/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.room

import kotlin.reflect.KClass

/**
 * Defines the [androidx.room.RoomDatabaseConstructor] that will instantiate the Room generated
 * implementation of the annotated `@Database`.
 *
 * A [androidx.room.RoomDatabase] database definition must be annotated with this annotation if it
 * is located in a common source set on a Kotlin Multiplatform project such that at runtime the
 * implementation generated by the annotation processor can be used. The [value] must be an 'expect
 * object' that implements [androidx.room.RoomDatabaseConstructor].
 *
 * Example usage:
 * ```
 * @Database(version = 1, entities = [Song::class, Album::class])
 * @ConstructedBy(MusicDatabaseConstructor::class)
 * abstract class MusicDatabase : RoomDatabase
 *
 * expect object MusicDatabaseConstructor : RoomDatabaseConstructor<MusicDatabase>
 * ```
 *
 * @see androidx.room.RoomDatabaseConstructor
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
actual annotation class ConstructedBy(
    /**
     * The 'expect' declaration of an 'object' that implements
     * [androidx.room.RoomDatabaseConstructor] and is able to instantiate a
     * [androidx.room.RoomDatabase].
     */
    actual val value: KClass<*>
)
