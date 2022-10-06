/*
 * Copyright 2018 The Android Open Source Project
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

package androidx.room.solver.shortcut.binder

import androidx.room.compiler.codegen.XPropertySpec
import androidx.room.ext.N
import androidx.room.solver.CodeGenScope
import androidx.room.solver.shortcut.result.DeleteOrUpdateMethodAdapter
import androidx.room.vo.ShortcutQueryParameter
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.TypeSpec

/**
 * Binder that knows how to write instant (blocking) delete and update methods.
 */
class InstantDeleteOrUpdateMethodBinder(
    adapter: DeleteOrUpdateMethodAdapter?
) : DeleteOrUpdateMethodBinder(adapter) {

    override fun convertAndReturn(
        parameters: List<ShortcutQueryParameter>,
        adapters: Map<String, Pair<XPropertySpec, TypeSpec>>,
        dbField: FieldSpec,
        scope: CodeGenScope
    ) {
        scope.builder().apply {
            addStatement("$N.assertNotSuspendingTransaction()", dbField)
        }
        adapter?.createDeleteOrUpdateMethodBody(
            parameters = parameters,
            adapters = adapters,
            dbField = dbField,
            scope = scope
        )
    }
}