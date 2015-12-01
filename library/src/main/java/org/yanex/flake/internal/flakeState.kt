package org.yanex.flake.internal

import org.yanex.flake.Flake
import org.yanex.flake.FlakeHolder
import java.lang.ref.SoftReference

open class FlakeState(val flake: Flake<*>)

class RetainedState(flake: Flake<*>, val holder: FlakeHolder) : FlakeState(flake)

internal class SoftReferenceState(flake: Flake<*>, val holder: SoftReference<FlakeHolder>) : FlakeState(flake)