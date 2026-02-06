package io.github.chrisimx.esclkt


import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.xmlStreaming

inline fun <reified T : Any> XML.decodeFromString(
    string: String
): T {
    return decodeFromReader<T>(ESCLNamespaceOverrideReader(string))
}
class ESCLNamespaceOverrideReader(
    xmlString: String,
) : XmlReader by xmlStreaming.newReader(xmlString) {
    override fun toString(): String = "ManualNamespaceXmlReader(namespaceURI=\"$namespaceURI\")"
    private val namespaceDepthStack = ArrayDeque<Pair<String, Int>>()

    override val namespaceURI: String
        get() {
            while ((namespaceDepthStack.lastOrNull()?.second ?: 0) > depth) {
                namespaceDepthStack.removeLast()
            }
            val newNamespace = when (localName) {
                "scan" -> NS_SCAN
                "pwg" -> NS_PWG
                else -> null
            }
            newNamespace?.let {
                namespaceDepthStack.addLast(it to depth)
            }
            return namespaceDepthStack.last().first
        }

}
