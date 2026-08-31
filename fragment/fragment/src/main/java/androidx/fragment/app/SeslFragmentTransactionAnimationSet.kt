/*
 * Copyright 2025 The Android Open Source Project
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
package androidx.fragment.app


import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.fragment.R

enum class SeslFragmentTransactionAnimationSet(
    @param:AnimRes @param:AnimatorRes val enter: Int,
    @param:AnimRes @param:AnimatorRes val exit: Int,
    @param:AnimRes @param:AnimatorRes val popEnter: Int,
    @param:AnimRes @param:AnimatorRes val popExit: Int
) {
    Horizontal(
        R.animator.sesl_fragment_open_enter,
        R.animator.sesl_fragment_open_exit,
        R.animator.sesl_fragment_close_enter,
        R.animator.sesl_fragment_close_exit
    ),
    HorizontalForRTL(
        R.animator.sesl_fragment_open_enter_rtl,
        R.animator.sesl_fragment_open_exit_rtl,
        R.animator.sesl_fragment_close_enter_rtl,
        R.animator.sesl_fragment_close_exit_rtl
    );

    companion object {
        @JvmStatic
        fun isFragmentAnimationRes(@AnimRes @AnimatorRes resId: Int): Boolean {
            return entries.any {
                it.enter == resId || it.exit == resId || it.popEnter == resId || it.popExit == resId
            }
        }

        @JvmStatic
        fun isOpenEnter(@AnimRes @AnimatorRes resId: Int): Boolean {
            return entries.any { it.enter == resId }
        }

        @JvmStatic
        fun isOpenExit(@AnimRes @AnimatorRes resId: Int): Boolean {
            return entries.any { it.exit == resId }
        }

        @JvmStatic
        fun isPopEnter(@AnimRes @AnimatorRes resId: Int): Boolean {
            return entries.any { it.popEnter == resId }
        }

        @JvmStatic
        fun isPopExit(@AnimRes @AnimatorRes resId: Int): Boolean {
            return entries.any { it.popExit == resId }
        }
    }
}