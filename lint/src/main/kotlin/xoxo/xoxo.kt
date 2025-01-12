// Source: https://github.com/martinbonnin/xoxo
// Lint is not able to add transitive dependencies, leading to the following runtime error:
// ClassNotFoundException: xoxo.XoxoKt

package xoxo

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.w3c.dom.Text
import java.util.LinkedList

public sealed interface XmlNode {
    public fun asDomNode(): Node
}

public fun Node.toXmlNode(): XmlNode? = when (this) {
    is Element -> XmlElement(this)
    is Text -> XmlText(this)
    else -> null
}

public class XmlElement internal constructor(private val element: Element) : XmlNode {
    public val name: String get() = element.tagName
    public val children: List<XmlNode> get() = element.childNodes.toList().mapNotNull { it.toXmlNode() }
    public val childElements: List<XmlElement> get() = children.filterIsInstance<XmlElement>()

    override fun asDomNode(): Element = element
}

public class XmlText internal constructor(private val text: Text) : XmlNode {
    public val content: String get() = text.textContent

    override fun asDomNode(): Text = text
}

public class XmlDocument internal constructor(private val document: Document) {
    public val root: XmlElement get() = XmlElement(document.documentElement)
}

private fun NodeList.toList(): List<Node> = 0.until(length).map(::item)

public fun XmlNode.walk(): Sequence<XmlNode> {
    val stack: LinkedList<XmlNode> = LinkedList(listOf(this))
    return generateSequence {
        if (stack.isEmpty()) return@generateSequence null
        val element = stack.pop()
        if (element is XmlElement) stack.addAll(0, element.children)
        element
    }
}
