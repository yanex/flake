package org.yanex.flake.internal

import org.yanex.flake.Flake
import org.yanex.flake.FlakeHolder
import java.lang.ref.SoftReference

internal open class FlakeState(public val flake: Flake<*>)

internal class RetainedState(flake: Flake<*>, val holder: FlakeHolder) : FlakeState(flake)

internal class SoftReferenceState(flake: Flake<*>, val holder: SoftReference<FlakeHolder>) : FlakeState(flake)