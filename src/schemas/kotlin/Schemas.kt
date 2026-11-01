import com.mineinabyss.deeperworld.DeeperWorldConfig
import com.mineinabyss.jsonschema.writer.writeSchemas

fun main(args: Array<String>) = writeSchemas(args) {
    schema("deeperworld/config", listOf("**/plugins/DeeperWorld/config.yml"), DeeperWorldConfig.serializer())
}
