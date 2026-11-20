package edu.missouristate.mars.book

import com.intellij.ui.dsl.listCellRenderer.textListCellRenderer
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.geom.Line2D
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BookMarsBotCaptureTest {
    private val east = BookMarsBotView(x = 16, heading = 90, penDown = true, trailVertices = listOf(BookBotPoint(), BookBotPoint(16, 0)))
    private val square = BookMarsBotView(trailVertices = listOf(BookBotPoint(), BookBotPoint(16, 0), BookBotPoint(16, 16), BookBotPoint(0, 16), BookBotPoint()))

    private fun board(expected: BookMarsBotView, painted: BookMarsBotView = expected, extra: Boolean = false) = object : JPanel() {
        init {
            setSize(420, 400)
            getAccessibleContext().accessibleName = "MarsBot board"
            getAccessibleContext().accessibleDescription = expected.positionText + "   " + expected.movementText
        }
        override fun isShowing() = true
        override fun getLocationOnScreen() = Point(100, 200)
        override fun paintComponent(graphics: Graphics) {
            val g = graphics.create() as Graphics2D
            try {
                g.color = Color.WHITE; g.fillRect(0, 0, width, height)
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g.color = Color.BLACK; g.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                // Rectangular 420 x 400 panel: 384-pixel square at (18, 8), world extent 511.
                painted.trailVertices.zipWithNext().forEach { (a, b) ->
                    g.draw(Line2D.Double(18 + a.x * 384.0 / 511, 8 + a.y * 384.0 / 511, 18 + b.x * 384.0 / 511, 8 + b.y * 384.0 / 511))
                }
                if (extra) g.fillRect(200, 200, 4, 4)
            } finally { g.dispose() }
        }
    }

    @Test
    fun `world projection follows native board bounds and excludes marker occlusion`() {
        val p = BookBotProjection(420, 400, east)
        assertEquals(18.0 to 8.0, p.project(BookBotPoint()))
        assertEquals(402.0 to 392.0, p.project(BookBotPoint(511, 511)))
        val marker = p.project(BookBotPoint(16, 0))
        assertTrue(p.markerAt(marker.first, marker.second))
        assertFalse(p.markerAt(200.0, 200.0))
        assertTrue(p.visibleCenters().isNotEmpty())
        assertTrue(p.detailRectangle().contains(18, 8))
        assertTrue(p.detailRectangle().contains(30, 8))
        assertEquals(0, BookBotProjection(400, 400, square).detailRectangle().x)
    }

    @Test
    fun `native first side corners and closed square are observed without simulator access`() {
        val south = east.copy(x = 16, y = 16, heading = 180, trailVertices = square.trailVertices.take(3))
        for (view in listOf(east, east.copy(moving = true), south, square)) {
            val result = checkNotNull(observeBookBotBoard(board(view), view))
            assertTrue(result.samples.isNotEmpty())
            assertEquals(0, result.projection.outsideTrailInk(BufferedImage(420, 400, BufferedImage.TYPE_INT_RGB).also { image ->
                val graphics = image.createGraphics(); try { board(view).paint(graphics) } finally { graphics.dispose() }
            }))
        }
    }

    @Test
    fun `wrong native metadata missing path and unexpected strokes fail`() {
        assertNull(observeBookBotBoard(board(east, east.copy(trailVertices = listOf(BookBotPoint(), BookBotPoint(0, 16)))), east))
        assertNull(observeBookBotBoard(board(east, extra = true), east))
        val component = board(east)
        component.getAccessibleContext().accessibleDescription = "Position (15, 0)"
        assertNull(observeBookBotBoard(component, east))
        component.getAccessibleContext().accessibleDescription = east.positionText + "   " + east.movementText
        component.getAccessibleContext().accessibleName = "Other board"
        assertNull(observeBookBotBoard(component, east))
        component.getAccessibleContext().accessibleName = "MarsBot board"; component.setSize(1, 1)
        assertNull(observeBookBotBoard(component, east))
    }

    @Test
    fun `screen evidence rejects clipped missing and extra photographed ink`() {
        val component = board(square); val observed = checkNotNull(observeBookBotBoard(component, square))
        val image = BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        try { graphics.translate(100, 200); component.paint(graphics) } finally { graphics.dispose() }
        val evidence = observed.screenEvidence(image, Rectangle(0, 0, 800, 800))
        assertEquals(512, evidence["worldSize"])
        assertThrows<IllegalArgumentException> { observed.screenEvidence(image, Rectangle(500, 500, 800, 800)) }
        image.setRGB(300, 400, Color.BLACK.rgb)
        assertThrows<IllegalStateException> { observed.screenEvidence(image, Rectangle(0, 0, 800, 800)) }
        image.setRGB(300, 400, Color.WHITE.rgb)
        val sample = observed.samples.first()
        image.setRGB(100 + sample.x, 200 + sample.y, Color.WHITE.rgb)
        assertThrows<IllegalStateException> { observed.screenEvidence(image, Rectangle(0, 0, 800, 800)) }
    }

    @Test
    fun `native tool binds execution position controls rate and port descriptions`() {
        val root = JPanel()
        val selector = object : JComboBox<String>(arrayOf("Other (#1)", "Book figure: bot (#2)")) { override fun isShowing() = true }
        selector.renderer = textListCellRenderer { it: String -> it }
        val button = object : JButton("Disconnect") { override fun isShowing() = true }
        val clock = object : JTextField("64") {
            override fun isShowing() = true
            override fun getLocationOnScreen() = Point(100, 100)
        }.also { it.setSize(100, 25) }
        fun label(text: String) = object : JLabel(text) {
            override fun isShowing() = true
            override fun getLocationOnScreen() = Point(100, 140)
        }.also { it.setSize(250, 20) }
        val status = label(east.positionText); val movement = label(east.movementText)
        val ports = label("MMIO base 0xffff8010").also { it.toolTipText = "Heading 0xffff8010, pen 0xffff8020, X 0xffff8030, Y 0xffff8040, move 0xffff8050" }
        listOf(selector, button, clock, status, movement, ports, board(east)).forEach(root::add)
        assertNull(observeBookMarsBot(root, east, "Book figure: bot"))
        val observed = checkNotNull(observeBookMarsBot(root, east, "Book figure: bot"))
        assertEquals(4, observed.rows.size)
        val detail = observed.board.projection.detailRectangle()
        assertTrue(Rectangle(0, 0, 420, 400).contains(detail))
        assertTrue(detail.width < 100 && detail.height < 100)
        assertNotNull(observed.board)
        assertNull(observeBookMarsBot(root, east, "Unowned"))
        for (mutation in listOf<() -> Unit>({ status.text = "Position (15, 0)" }, { movement.text = "Pen up   Stopped" },
                { clock.text = "65" }, { clock.isEnabled = false }, { button.text = "Connect" }, { ports.toolTipText = "Wrong port" })) {
            mutation(); assertNull(observeBookMarsBot(root, east, "Book figure: bot"))
            status.text = east.positionText; movement.text = east.movementText; clock.text = "64"; clock.isEnabled = true; button.text = "Disconnect"
        }
    }

    @Test
    fun `plan separates real backstep from forward steps and restricts bot view modes`() {
        val figure = BookCaptureFigure(id = "bot", source = "bot.asm", sourceSha256 = "a".repeat(64), checkpoint = "east_stop", backstepFrom = "east_stopped", marsBotView = east.copy(moving = true))
        val root = Path.of("/tmp/bot")
        figure.validate(root)
        for (bad in listOf(figure.copy(backstepFrom = "east_stop"), figure.copy(backstepFrom = ""), figure.copy(stepFrom = listOf("before")),
            figure.copy(breakpointCondition = "x"), figure.copy(memoryView = BookMemoryView("0x10010000")), figure.copy(digitalLabView = BookDigitalLabView()),
            figure.copy(expectedRows = listOf(BookExpectedRow("State", listOf("16")))), figure.copy(mode = "editor", checkpoint = ""),
            figure.copy(marsBotView = east.copy(x = 512)), figure.copy(marsBotView = east.copy(y = -1)), figure.copy(marsBotView = east.copy(heading = 360)),
            figure.copy(marsBotView = east.copy(instructionsPerTick = 0)), figure.copy(marsBotView = east.copy(trailVertices = emptyList())),
            figure.copy(marsBotView = east.copy(trailVertices = listOf(BookBotPoint(), BookBotPoint()))), figure.copy(marsBotView = east.copy(trailVertices = listOf(BookBotPoint(), BookBotPoint(512, 0)))))) {
            assertThrows<IllegalArgumentException> { bad.validate(root) }
        }
        assertTrue(isBookBotInk(Color.BLACK.rgb))
        for (color in listOf(Color.WHITE, Color.BLUE, Color(120, 0, 0), Color(0, 120, 0), Color(0, 0, 120))) assertFalse(isBookBotInk(color.rgb))
    }
}
