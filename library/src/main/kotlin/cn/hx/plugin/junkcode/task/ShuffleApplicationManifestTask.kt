package cn.hx.plugin.junkcode.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Element
import java.io.File
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.Collections
import java.util.Random
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * 打乱AndroidManifest.xml application下元素顺序
 */
@CacheableTask
abstract class ShuffleApplicationManifestTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputManifest: RegularFileProperty

    @get:OutputFile
    abstract val outputManifest: RegularFileProperty

    @get:Input
    abstract val variantName: Property<String>

    @TaskAction
    fun shuffleApplicationChildren() {
        val inputFile = inputManifest.get().asFile
        val outputFile = outputManifest.get().asFile
        val document = createDocumentBuilderFactory()
            .newDocumentBuilder()
            .parse(inputFile)
        val application = document.documentElement
            .getElementsByTagName("application")
            .let { nodes -> if (nodes.length == 0) null else nodes.item(0) as? Element }

        if (application == null) {
            outputFile.parentFile.mkdirs()
            inputFile.copyTo(outputFile, overwrite = true)
            logger.warn("{}: No application element found; copied merged manifest unchanged", path)
            return
        }

        val originalNodes = application.childNodes.let { childNodes ->
            (0 until childNodes.length).map(childNodes::item)
        }
        val originalElements = originalNodes.filterIsInstance<Element>()
        val shuffledElements = originalElements.toMutableList()
        if (shuffledElements.size > 1) {
            shuffledElements.shuffle(Random(createSeed(inputFile, variantName.get())))
            if (shuffledElements.indices.all { shuffledElements[it] === originalElements[it] }) {
                Collections.rotate(shuffledElements, 1)
            }
        }

        var elementIndex = 0
        while (application.hasChildNodes()) {
            application.removeChild(application.firstChild)
        }
        originalNodes.forEach { node ->
            application.appendChild(if (node is Element) shuffledElements[elementIndex++] else node)
        }

        outputFile.parentFile.mkdirs()
        TransformerFactory.newInstance().apply {
            setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        }.newTransformer().apply {
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            setOutputProperty(OutputKeys.INDENT, "no")
        }.transform(DOMSource(document), StreamResult(outputFile))

        logger.lifecycle(
            "{}: Shuffled {} direct application child elements",
            path,
            shuffledElements.size
        )
    }

    private fun createSeed(inputFile: File, variant: String): Long {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(inputFile.readBytes() + variant.toByteArray(Charsets.UTF_8))
        return ByteBuffer.wrap(digest).long
    }

    private fun createDocumentBuilderFactory(): DocumentBuilderFactory =
        DocumentBuilderFactory.newInstance().apply {
            setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
            setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
            isXIncludeAware = false
            isExpandEntityReferences = false
        }
}