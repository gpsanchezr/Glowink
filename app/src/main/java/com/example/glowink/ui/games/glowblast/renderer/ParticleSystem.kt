package com.example.glowink.ui.games.glowblast.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin

/**
 * Reutilización de objetos Partícula (Object Pooling) para proteger los 60 FPS sin asignaciones continuas de memoria.
 */
class GlowParticle {
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var radius: Float = 4f
    var alpha: Float = 1.0f
    var color: Color = Color.White
    var maxLifeMs: Long = 500L
    var ageMs: Long = 0L
    var active: Boolean = false

    fun init(x: Float, y: Float, vx: Float, vy: Float, radius: Float, color: Color, maxLifeMs: Long) {
        this.x = x
        this.y = y
        this.vx = vx
        this.vy = vy
        this.radius = radius
        this.color = color
        this.maxLifeMs = maxLifeMs
        this.ageMs = 0L
        this.active = true
        this.alpha = 1.0f
    }

    fun update(deltaSec: Float): Boolean {
        if (!active) return false
        x += vx * deltaSec
        y += vy * deltaSec
        ageMs += (deltaSec * 1000f).toLong()
        alpha = (1.0f - ageMs.toFloat() / maxLifeMs.toFloat()).coerceIn(0f, 1f)

        if (ageMs >= maxLifeMs) {
            active = false
        }
        return active
    }
}

/**
 * Pool de Partículas Neón pre-asignadas en memoria para rendimiento sostenido a 60 FPS.
 */
object ParticlePool {
    private const val POOL_SIZE = 120
    private val pool = Array(POOL_SIZE) { GlowParticle() }

    fun spawnParticle(
        x: Float,
        y: Float,
        vx: Float,
        vy: Float,
        radius: Float = 5f,
        color: Color = Color.White,
        maxLifeMs: Long = 400L
    ) {
        val freeParticle = pool.firstOrNull { !it.active } ?: pool[0]
        freeParticle.init(x, y, vx, vy, radius, color, maxLifeMs)
    }

    fun updateAndDraw(drawScope: DrawScope, deltaSec: Float) {
        for (particle in pool) {
            if (particle.active) {
                val isStillAlive = particle.update(deltaSec)
                if (isStillAlive) {
                    drawScope.drawCircle(
                        color = particle.color.copy(alpha = particle.alpha),
                        radius = particle.radius,
                        center = Offset(particle.x, particle.y)
                    )
                }
            }
        }
    }

    fun spawnExplosionBurst(centerX: Float, centerY: Float, color: Color) {
        repeat(20) { i ->
            val angleRad = Math.toRadians((i * 18.0))
            val speed = (80..220).random().toFloat()
            val vx = cos(angleRad).toFloat() * speed
            val vy = sin(angleRad).toFloat() * speed
            spawnParticle(centerX, centerY, vx, vy, radius = (3..8).random().toFloat(), color = color, maxLifeMs = (300..600).random().toLong())
        }
    }
}
