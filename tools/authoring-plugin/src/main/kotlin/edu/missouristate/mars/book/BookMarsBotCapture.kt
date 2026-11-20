package edu.missouristate.mars.book

import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.JBUI
import java.awt.Rectangle
import java.awt.geom.Line2D
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JTextField
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.floor
import kotlin.math.roundToInt

/** Integer world coordinates, distinct from the board's scaled screen coordinates. */
data class BookBotPoint(val x: Int = 0, val y: Int = 0)
data class BookMarsBotView(
    val x: Int = 0, val y: Int = 0, val heading: Int = 0,
    val moving: Boolean = false, val penDown: Boolean = false,
    val instructionsPerTick: Int = 64,
    val trailVertices: List<BookBotPoint> = emptyList(),
) {
    fun validate() {
        require(x in 0..511 && y in 0..511 && heading in 0..359) { "Invalid MarsBot position or heading" }
        require(instructionsPerTick in 1..1000000) { "Invalid native MarsBot rate" }
        require(trailVertices.size in 2..32 && trailVertices.all { it.x in 0..511 && it.y in 0..511 }) { "A bounded visible trail is required" }
        require(trailVertices.zipWithNext().all { (a, b) -> a != b }) { "Empty trail segment" }
    }
    val positionText get() = "Position ($x, $y)   Heading $heading°"
    val movementText get() = (if (penDown) "Pen down" else "Pen up") + "   " + (if (moving) "Moving" else "Stopped")
}

internal fun isBookBotInk(rgb: Int) = (rgb ushr 16 and 255) < 120 && (rgb ushr 8 and 255) < 120 && (rgb and 255) < 120

/** Geometry follows the native board's public visual contract; no simulator state is accessed. */
internal class BookBotProjection(val width: Int, val height: Int, val view: BookMarsBotView) {
    val side = minOf(width, height) - 2 * JBUI.scale(8)
    val left = (width - side) / 2.0
    val top = (height - side) / 2.0
    private val scale = side / 511.0
    fun project(p: BookBotPoint) = (left + p.x * scale) to (top + p.y * scale)
    private val segments = view.trailVertices.zipWithNext().map { (a, b) ->
        val (ax, ay) = project(a); val (bx, by) = project(b)
        Line2D.Double(ax, ay, bx, by)
    }
    private val marker = Path2D.Double().apply {
        val (x, y) = project(BookBotPoint(view.x, view.y))
        val angle = Math.toRadians(view.heading.toDouble())
        fun vertex(dx: Double, dy: Double): Pair<Double, Double> {
            val size = JBUIScale.scale(1f).toDouble()
            return (x + (dx * StrictMath.cos(angle) - dy * StrictMath.sin(angle)) * size) to
                (y + (dx * StrictMath.sin(angle) + dy * StrictMath.cos(angle)) * size)
        }
        val a = vertex(0.0, -7.0); val b = vertex(5.0, 5.0); val c = vertex(-5.0, 5.0)
        moveTo(a.first, a.second); lineTo(b.first, b.second); lineTo(c.first, c.second); closePath()
    }
    fun detailRectangle(): Rectangle {
        val points = view.trailVertices.map(::project) + project(BookBotPoint(view.x, view.y))
        val x = floor(points.minOf { it.first } - JBUI.scale(10)).toInt()
        val y = floor(points.minOf { it.second } - JBUI.scale(10)).toInt()
        val right = ceil(points.maxOf { it.first } + JBUI.scale(10)).toInt()
        val bottom = ceil(points.maxOf { it.second } + JBUI.scale(10)).toInt()
        return Rectangle(x, y, right - x, bottom - y).intersection(Rectangle(0, 0, width, height))
    }
    fun markerAt(x: Double, y: Double) = marker.intersects(x - 1, y - 1, 2.0, 2.0)
    fun distance(x: Double, y: Double) = segments.minOf { it.ptSegDist(x, y) }
    fun outsideTrailInk(image: BufferedImage): Int {
        var count = 0
        for (y in (top + 2).toInt() until (top + side - 1).toInt()) {
            for (x in (left + 2).toInt() until (left + side - 1).toInt()) {
                if (isBookBotInk(image.getRGB(x, y)) && !markerAt(x.toDouble(), y.toDouble()) && distance(x.toDouble(), y.toDouble()) > JBUI.scale(2)) count++
            }
        }
        return count
    }
    fun visibleCenters(): List<Pair<Double, Double>> = segments.flatMap { line ->
        val count = ceil(hypot(line.x2 - line.x1, line.y2 - line.y1) * 2).toInt().coerceAtLeast(1)
        (0..count).map { i ->
            val ratio = i.toDouble() / count
            (line.x1 + (line.x2 - line.x1) * ratio) to (line.y1 + (line.y2 - line.y1) * ratio)
        }.filterNot { (x, y) -> markerAt(x, y) }
    }
}

internal data class BookBotInkSample(val x: Int, val y: Int, val nativeRgb: Int)
internal data class BookObservedBotBoard(val bounds: Rectangle, val projection: BookBotProjection, val samples: List<BookBotInkSample>) {
    fun screenEvidence(image: BufferedImage, viewport: Rectangle): Map<String, Any> {
        val relative = Rectangle(bounds).apply { translate(-viewport.x, -viewport.y) }
        require(Rectangle(0, 0, image.width, image.height).contains(relative)) { "MarsBot board is outside the photograph" }
        val board = image.getSubimage(relative.x, relative.y, relative.width, relative.height)
        check(projection.outsideTrailInk(board) == 0) { "Photographed ink is outside the expected trail" }
        val evidence = samples.map { sample ->
            val rgb = board.getRGB(sample.x, sample.y) and 0xffffff
            check(isBookBotInk(rgb)) { "A photographed trail sample is missing" }
            mapOf("x" to relative.x + sample.x, "y" to relative.y + sample.y, "nativeRgb" to sample.nativeRgb, "screenRgb" to rgb)
        }
        val detail = projection.detailRectangle().also { it.translate(bounds.x, bounds.y) }
        return mapOf("boardBox" to captureBox(bounds, viewport), "worldSize" to 512,
            "plot" to mapOf("left" to projection.left, "top" to projection.top, "side" to projection.side),
            "detailBox" to captureBox(detail, viewport),
            "visibleTrailSamples" to evidence, "unexpectedNativeInk" to 0, "unexpectedScreenInk" to 0,
            "markerOcclusionExcluded" to true, "borderExcludedFromUnexpectedInk" to 2)
    }
}

internal fun observeBookBotBoard(component: JComponent, view: BookMarsBotView): BookObservedBotBoard? {
    if (component.accessibleContext?.accessibleName != "MarsBot board" ||
        component.accessibleContext?.accessibleDescription != view.positionText + "   " + view.movementText ||
        component.width < 64 || component.height < 64) return null
    val projection = BookBotProjection(component.width, component.height, view)
    val image = BufferedImage(component.width, component.height, BufferedImage.TYPE_INT_RGB)
    val graphics = image.createGraphics()
    try { component.paint(graphics) } finally { graphics.dispose() }
    if (projection.outsideTrailInk(image) != 0) return null
    val samples = projection.visibleCenters().map { (x, y) ->
        val px = x.roundToInt(); val py = y.roundToInt()
        val match = (-1..1).flatMap { dy -> (-1..1).map { dx -> (px + dx) to (py + dy) } }
            .filter { (sx, sy) -> sx in 0 until image.width && sy in 0 until image.height &&
                hypot(sx - x, sy - y) <= 1.25 && !projection.markerAt(sx.toDouble(), sy.toDouble()) }
            .firstOrNull { (sx, sy) -> isBookBotInk(image.getRGB(sx, sy)) } ?: return null
        BookBotInkSample(match.first, match.second, image.getRGB(match.first, match.second) and 0xffffff)
    }.distinctBy { it.x to it.y }
    if (samples.isEmpty()) return null
    return BookObservedBotBoard(Rectangle(component.locationOnScreen, component.size), projection, samples)
}

internal data class BookObservedMarsBot(val rows: List<BookUiRow>, val selectedExecution: String, val board: BookObservedBotBoard)

internal fun observeBookMarsBot(root: JComponent, view: BookMarsBotView, execution: String): BookObservedMarsBot? {
    val components = captureComponents(root).filter { it.isShowing }.toList()
    val selected = selectBookToolExecution(components, execution) ?: return null
    if (components.filterIsInstance<JButton>().none { it.text == "Disconnect" && it.isEnabled }) return null
    val clock = components.filterIsInstance<JTextField>().singleOrNull { it.text == view.instructionsPerTick.toString() && it.isEnabled } ?: return null
    fun label(text: String) = components.filterIsInstance<JLabel>().singleOrNull { it.text == text }
    val status = label(view.positionText) ?: return null
    val movement = label(view.movementText) ?: return null
    val ports = label("MMIO base 0xffff8010") ?: return null
    if (ports.toolTipText?.let { listOf("0xffff8010", "0xffff8020", "0xffff8030", "0xffff8040", "0xffff8050").all(it::contains) } != true) return null
    val boardComponent = components.filterIsInstance<JComponent>().singleOrNull { it.accessibleContext?.accessibleName == "MarsBot board" } ?: return null
    val board = observeBookBotBoard(boardComponent, view) ?: return null
    fun bounds(c: JComponent) = Rectangle(c.locationOnScreen, c.size)
    return BookObservedMarsBot(listOf(BookUiRow(view.positionText, bounds(status)), BookUiRow(view.movementText, bounds(movement)),
        BookUiRow("Instructions per tick: ${view.instructionsPerTick}", bounds(clock)), BookUiRow("MarsBot board", board.bounds)), selected, board)
}

internal fun bookBotDiagnostics(root: JComponent): List<String> = listOf("MarsBot diagnostics") + captureComponents(root).filter { it.isShowing }.mapNotNull {
    when (it) {
        is JLabel -> it.text
        is JTextField -> "Rate: ${it.text}; enabled=${it.isEnabled}"
        is JButton -> "Button: ${it.text}; enabled=${it.isEnabled}"
        is JComponent -> if (it.accessibleContext?.accessibleName == "MarsBot board") "Board: ${it.width}x${it.height}; ${it.accessibleContext?.accessibleDescription}" else null
        else -> null
    }
}.toList()
