package io.github.chrisimx.esclkt

import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Document
import org.w3c.dom.Element

object XmlHelpers {
    fun newDocument(): Document {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        return builder.newDocument()
    }

    fun Document.createElementWithText(tag: String, text: String): Element {
        val elem = this.createElement(tag)
        elem.textContent = text
        return elem
    }

    fun Element.addTextElement(tag: String, text: String?): Element? {
        if (text == null) return null

        val newChild = this.ownerDocument.createElementWithText(tag, text)
        this.appendChild(newChild)
        return newChild
    }
}
