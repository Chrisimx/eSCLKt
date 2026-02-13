package io.github.chrisimx.esclkt

import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.QName
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.core.XmlVersion
import nl.adaptivity.xmlutil.serialization.InputKind
import nl.adaptivity.xmlutil.serialization.OutputKind
import nl.adaptivity.xmlutil.serialization.UnknownChildHandler
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.structure.XmlDescriptor
import nl.adaptivity.xmlutil.xmlStreaming

data class UnknownInput(
    val inputKind: InputKind,
    val descriptor: XmlDescriptor,
    val name: QName?,
    val candidates: Collection<Any>
) {
    override fun toString(): String {
        return "UnknownInput($inputKind, $name)"
    }
}

class ESCLXml {

    // To keep track of unknown elements, attributes, ...
    val unknown = mutableListOf<UnknownInput>()

    @OptIn(ExperimentalXmlUtilApi::class)
    val xml: XML = XML {
        recommended() {
            defaultObjectOutputKind = OutputKind.Element
            defaultPrimitiveOutputKind = OutputKind.Element
            xmlVersion = XmlVersion.XML10
            xmlDeclMode = XmlDeclMode.Charset
            indentString = ""

            unknownChildHandler = UnknownChildHandler { input, inputKind, descriptor, name, candidates ->
                unknown.add(UnknownInput(inputKind, descriptor, name, candidates))
                emptyList()
            }
        }
    }

    inline fun <reified T : Any> decodeFromString(
        string: String
    ): T {
        unknown.clear()
        return xml.decodeFromReader<T>(ESCLQuirkFilteringReader(xmlStreaming.newReader(string)))
    }
}