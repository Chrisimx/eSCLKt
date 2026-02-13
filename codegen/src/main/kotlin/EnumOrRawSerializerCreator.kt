import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import java.io.OutputStreamWriter

class EnumOrRawSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return EnumOrRawProcessor(environment.codeGenerator, environment.logger)
    }
}

class EnumOrRawProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {

        logger.info("Starting EnumOrRawProcessor...")

        val symbols = resolver.getSymbolsWithAnnotation("io.github.chrisimx.esclkt.GenerateEnumOrRawSerializer")

        symbols.filterIsInstance<KSClassDeclaration>().forEach { enumClass ->
            if (!enumClass.classKind.name.equals("ENUM_CLASS")) return@forEach
            val packageName = enumClass.packageName.asString()
            val className = enumClass.simpleName.asString()

            val file = codeGenerator.createNewFile(
                Dependencies(false, enumClass.containingFile!!),
                packageName,
                "${className}DataSerializer"
            )

            OutputStreamWriter(file).use { writer ->
                writer.write(
                    """
                    package $packageName

                    import kotlinx.serialization.KSerializer
                    import kotlinx.serialization.Serializable
                    import kotlinx.serialization.builtins.ListSerializer

                    object ${className}DataSerializer : KSerializer<EnumOrRaw<$className>> by enumOrRawSerializer<$className>()

                    typealias ${className}EnumOrRaw = @Serializable(with = ${className}DataSerializer::class) EnumOrRaw<${className}>
                    """.trimIndent()
                )
            }
        }
        return emptyList()
    }
}

