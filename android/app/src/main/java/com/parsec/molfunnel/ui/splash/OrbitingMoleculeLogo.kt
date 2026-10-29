package com.parsec.molfunnel.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp as lerpOffset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

internal fun seg(u: Float, a: Float, b: Float) = ((u - a) / (b - a)).coerceIn(0f, 1f)
private fun ease(t: Float) = 1f - (1f - t).pow(3)
private fun smooth(t: Float) = t * t * (3f - 2f * t)
private fun rad(deg: Float) = Math.toRadians(deg.toDouble()).toFloat()

private fun orbitPoint(c: Offset, a: Float, b: Float, tiltDeg: Float, angleDeg: Float): Offset {
    val t = rad(angleDeg)
    val p = rad(tiltDeg)
    val x = a * cos(t)
    val y = b * sin(t)
    return Offset(c.x + x * cos(p) - y * sin(p), c.y + x * sin(p) + y * cos(p))
}

/** One drifting molecule dot: a looping Lissajous path (integer multipliers = seamless loop). */
private class Drifter(val kx: Int, val ky: Int, val px: Float, val py: Float, val ax: Float, val ay: Float)

// 0..5 ring vertices, 6..9 attached atoms, 10..12 orbit atoms, 13..20 ambient molecules
private val drifters = List(21) { i ->
    val r = Random(i * 7919 + 13)
    Drifter(
        r.nextInt(1, 4), r.nextInt(1, 4),
        r.nextFloat() * 6.283f, r.nextFloat() * 6.283f,
        0.45f + r.nextFloat() * 0.5f, 0.45f + r.nextFloat() * 0.5f
    )
}

/** unfold: 0 = loose drifting molecules, 1 = finished logo. */
@Composable
fun OrbitingMoleculeLogo(unfold: Float = 1f, modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "molecule")
    val angleA by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(4800, easing = LinearEasing)), label = "a")
    val angleB by inf.animateFloat(360f, 0f, infiniteRepeatable(tween(6800, easing = LinearEasing)), label = "b")
    val spin by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(18000, easing = LinearEasing)), label = "spin")
    val wobble by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "wobble")
    val clock by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(12000, easing = LinearEasing)), label = "clock")
    val pulse by inf.animateFloat(
        0.94f, 1.06f,
        infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Canvas(modifier) {
        val c = center
        val half = size.minDimension / 2f
        val d = size.minDimension * 0.55f           // disc diameter = design unit
        val u = unfold
        val th = rad(clock)
        val discR = d / 2f * ease(seg(u, 0f, 0.4f))

        // orbit geometry (tilt precesses slowly)
        val wob = sin(rad(wobble))
        val tilt1 = -35f + 10f * wob
        val tilt2 = 14f - 8f * wob
        val a1 = d * 0.38f; val b1 = d * 0.17f
        val a2 = d * 0.37f; val b2 = d * 0.15f

        fun drift(i: Int): Offset {
            val dr = drifters[i]
            return Offset(
                c.x + dr.ax * half * 0.95f * cos(dr.kx * th + dr.px),
                c.y + dr.ay * half * 0.95f * sin(dr.ky * th + dr.py)
            )
        }

        // entity i drifts freely, then glides to its target (staggered by index)
        fun place(i: Int, target: Offset): Offset {
            val st = 0.10f + 0.02f * min(i, 12)
            return lerpOffset(drift(i), target, ease(seg(u, st, st + 0.45f)))
        }

        // molecule transform: spin + pulse around the center
        val hr = d * 0.14f
        fun mol(p: Offset, k: Float = 1f): Offset {
            val dx = (p.x - c.x) * pulse * k
            val dy = (p.y - c.y) * pulse * k
            val r = rad(spin)
            return Offset(c.x + dx * cos(r) - dy * sin(r), c.y + dx * sin(r) + dy * cos(r))
        }

        val base = (0..5).map { k ->
            Offset(c.x + hr * cos(rad(-90f + 60f * k)), c.y + hr * sin(rad(-90f + 60f * k)))
        }
        val subK = listOf(0, 5, 2, 3)
        val ring = List(6) { k -> place(k, mol(base[k])) }
        val subs = List(4) { j -> place(6 + j, mol(base[subK[j]], 1.75f)) }
        val atomA1 = place(10, orbitPoint(c, a1, b1, tilt1, angleA))
        val atomA2 = place(11, orbitPoint(c, a1, b1, tilt1, angleA + 180f))
        val atomB = place(12, orbitPoint(c, a2, b2, tilt2, angleB))
        val rc = Offset(ring.map { it.x }.average().toFloat(), ring.map { it.y }.average().toFloat())

        val bondP = ease(seg(u, 0.6f, 1f))
        val orbitE = ease(seg(u, 0.3f, 0.9f))
        val sweep = 360f * smooth(seg(u, 0.25f, 0.75f))
        val trailA = seg(u, 0.75f, 1f)
        val ambient = 1f - seg(u, 0.3f, 0.8f)
        val bondW = d * 0.016f

        // everything that is drawn, in one colour (white on the disc, dark outside it)
        val scene: DrawScope.(Color) -> Unit = { col ->

            // ambient drifting molecules, fade out as the logo forms
            if (ambient > 0f) {
                val amb = (13..20).map { drift(it) }
                for (i in 0 until amb.size - 1) {
                    if ((amb[i] - amb[i + 1]).getDistance() < d * 0.55f) {
                        drawLine(col.copy(alpha = 0.35f * ambient), amb[i], amb[i + 1], d * 0.006f)
                    }
                }
                amb.forEachIndexed { i, p ->
                    drawCircle(col.copy(alpha = 0.7f * ambient), d * (0.012f + 0.005f * (i % 4)), p)
                }
            }

            // orbits: ribbons that unfold (thick and flat, then thin and tilted)
            fun orbitArc(a: Float, b: Float, tilt: Float, wid: Float) {
                val ac = a * (0.6f + 0.4f * orbitE)
                val bc = max(b * orbitE, 0.5f)
                rotate(tilt + (1f - orbitE) * 110f, c) {
                    drawArc(
                        color = col,
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(c.x - ac, c.y - bc),
                        size = Size(ac * 2f, bc * 2f),
                        style = Stroke(wid * (1f + 3.5f * (1f - orbitE)), cap = StrokeCap.Round)
                    )
                }
            }
            orbitArc(a1, b1, tilt1, d * 0.012f)
            orbitArc(a2, b2, tilt2, d * 0.0096f)

            // ring edges grow between the settled vertices
            for (k in 0..5) {
                drawLine(col, ring[k], lerpOffset(ring[k], ring[(k + 1) % 6], bondP), bondW, StrokeCap.Round)
                drawCircle(col.copy(alpha = 1f - bondP), d * 0.014f, ring[k])   // vertex dot fades out
            }
            // inner double-bond lines
            val dbl = seg(bondP, 0.5f, 1f)
            if (dbl > 0f) {
                for (k in listOf(1, 3, 5)) {
                    val p1 = lerpOffset(ring[k], rc, 0.22f)
                    val p2 = lerpOffset(ring[(k + 1) % 6], rc, 0.22f)
                    drawLine(
                        col.copy(alpha = dbl),
                        lerpOffset(p1, p2, 0.18f), lerpOffset(p2, p1, 0.18f),
                        bondW * 0.6f, StrokeCap.Round
                    )
                }
            }
            // attached atoms and their bonds
            for (j in 0..3) {
                val v = ring[subK[j]]
                drawLine(col, v, lerpOffset(v, subs[j], bondP), bondW, StrokeCap.Round)
                drawCircle(col, d * 0.035f * (0.5f + 0.5f * ease(seg(u, 0.2f, 0.7f))), subs[j])
            }

            // orbit atoms with comet trails
            fun atomWithTrail(p: Offset, angle: Float, a: Float, b: Float, tilt: Float, r: Float, dir: Float) {
                for (k in 12 downTo 1) {
                    drawCircle(
                        col.copy(alpha = (1f - k / 13f) * 0.55f * trailA),
                        r * (1f - k * 0.05f),
                        orbitPoint(c, a, b, tilt, angle - dir * k * 5f)
                    )
                }
                drawCircle(col, r, p)
            }
            atomWithTrail(atomA1, angleA, a1, b1, tilt1, d * 0.032f, 1f)
            atomWithTrail(atomA2, angleA + 180f, a1, b1, tilt1, d * 0.024f, 1f)
            atomWithTrail(atomB, angleB, a2, b2, tilt2, d * 0.028f, -1f)
        }

        val dark = Color(0xFF111111)
        val disc = Path().apply { addOval(Rect(c, discR)) }
        drawCircle(dark, discR, c)
        clipPath(disc) { scene(Color.White) }                       // white on the disc
        clipPath(disc, ClipOp.Difference) { scene(dark) }           // dark on the white screen
    }
}
