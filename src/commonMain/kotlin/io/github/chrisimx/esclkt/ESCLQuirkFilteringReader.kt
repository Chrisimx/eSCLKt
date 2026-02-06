package io.github.chrisimx.esclkt


import nl.adaptivity.xmlutil.XmlDelegatingReader
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.xmlStreaming

inline fun <reified T : Any> XML.decodeFromString(
    string: String
): T {
    return decodeFromReader<T>(ESCLQuirkFilteringReader(xmlStreaming.newReader(string)))
}
class ESCLQuirkFilteringReader(
    reader: XmlReader
) : XmlDelegatingReader(reader) {

    override val prefix: String
        get() {
            if (localName == "ContentType") { // Because canon-mf628cw-caps.xml uses scan:ContentType instead of pwg:ContentType
                return "pwg"
            }
            return super.prefix
        }

    override val namespaceURI: String
        get() {
            if (localName == "ContentType") { // Because canon-mf628cw-caps.xml uses scan:ContentType instead of pwg:ContentType
                return NS_PWG
            }

            return when (prefix) {
                "scan" -> NS_SCAN
                "pwg" -> NS_PWG
                else -> NS_SCAN
            }
        }

    override val localName: String
        get() {
            return when (super.localName) {
                "SupportedIntent" -> "Intent" // Because kyocera-ecosys-m5521cdn-caps and ta-utax-p-c3567i-mfp-caps uses this in SupportedIntents
                else -> super.localName
            }
        }

}
